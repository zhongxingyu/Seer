 package org.thestaticvoid.iriverter;
 
 import java.io.*;
 import java.util.*;
 
 public class Converter extends Thread {
 	private List jobs, notSplitVideos;
 	private ProgressDialogInfo progressDialogInfo;
 	private ConverterOptions converterOptions;
 	private Process proc;
 	private boolean isCanceled;
 	private int exitCode;
 	
 	public Converter(List jobs, ProgressDialogInfo progressDialogInfo, ConverterOptions converterOptions) {
 		this.jobs = Converter.checkForOverwritingFiles(Converter.expandSingleJobsToMultiple(Converter.removeInvalidJobs(jobs, converterOptions), converterOptions));
 		this.progressDialogInfo = progressDialogInfo;
 		this.converterOptions = converterOptions;
 		isCanceled = false;
 		
 		notSplitVideos = new ArrayList();
 		
 		progressDialogInfo.setTotalJobs(this.jobs.size());
 	}
 	
 	public static List removeInvalidJobs(List jobs, ConverterOptions converterOptions) {
 		List newJobs = new ArrayList();
 		
 		for (int i = 0; i < jobs.size(); i++) {
 			boolean validOutput = true;
 			if (jobs.get(i) instanceof OutputVideoInfo) {
 				OutputVideoInfo outputVideoInfo = (OutputVideoInfo) jobs.get(i);
 				if (!outputVideoInfo.getOutputVideo().endsWith("." + converterOptions.getCurrentProfile().getWrapperFormat()) && !outputVideoInfo.getOutputVideo().equals(""))
 					outputVideoInfo.setOutputVideo(outputVideoInfo.getOutputVideo() + "." + converterOptions.getCurrentProfile().getWrapperFormat());
 				
 				if (!new File(outputVideoInfo.getOutputVideo()).getParentFile().exists())
 					validOutput = new File(outputVideoInfo.getOutputVideo()).getParentFile().mkdirs();
 				
 				validOutput = validOutput && new File(outputVideoInfo.getOutputVideo()).getParentFile().canWrite();
 			}
 			
 			if (jobs.get(i) instanceof SingleVideoInfo) {
 				SingleVideoInfo singleVideoInfo = (SingleVideoInfo) jobs.get(i);
				if (new File(singleVideoInfo.getInputVideo()).exists() && !singleVideoInfo.getOutputVideo().equals("") && validOutput)
 					newJobs.add(singleVideoInfo);
 			} else if (jobs.get(i) instanceof DirectoryInfo) {
 				DirectoryInfo directoryInfo = (DirectoryInfo) jobs.get(i);
 				if (new File(directoryInfo.getInputDirectory()).exists() && !directoryInfo.getOutputDirectory().equals(""))
 					if (!new File(directoryInfo.getOutputDirectory()).exists())
 						validOutput = new File(directoryInfo.getOutputDirectory()).mkdirs();
 				
 					if (validOutput)
 						newJobs.add(directoryInfo);
 			} else if (jobs.get(i) instanceof DVDInfo) {
 				DVDInfo dvdInfo = (DVDInfo) jobs.get(i);
				if (!dvdInfo.getDrive().equals("") && !dvdInfo.getOutputVideo().equals("") && validOutput)
 					newJobs.add(dvdInfo);
 			} else if (jobs.get(i) instanceof ManualSplitInfo) {
 				ManualSplitInfo manualSplitInfo = (ManualSplitInfo) jobs.get(i);
 				if (!manualSplitInfo.getVideo().equals("") && manualSplitInfo.getMarks().length > 2)
 					newJobs.add(manualSplitInfo);
 			} else if (jobs.get(i) instanceof JoinVideosInfo) {
 				JoinVideosInfo joinVideosInfo = (JoinVideosInfo) jobs.get(i);
 				if (joinVideosInfo.getInputVideos().length > 0 && validOutput)
 					newJobs.add(joinVideosInfo);
 			}
 		}
 			
 		return newJobs;
 	}
 	
 	public static List expandSingleJobsToMultiple(List jobs, ConverterOptions converterOptions) {
 		List newJobs = new ArrayList();
 		
 		for (int i = 0; i < jobs.size(); i++)	
 			if (jobs.get(i) instanceof DirectoryInfo)
 				newJobs.addAll(convertDirectoryToSingleVideos((DirectoryInfo) jobs.get(i), converterOptions));
 			else if (jobs.get(i) instanceof DVDInfo)
 				newJobs.addAll(separateDVDChaptersToSingleDVDJobs((DVDInfo) jobs.get(i)));
 			else if (jobs.get(i) instanceof ManualSplitInfo)
 				newJobs.addAll(separateMultipleSplitJobsToOneSplitJob((ManualSplitInfo) jobs.get(i)));
 			else
 				newJobs.add(jobs.get(i));
 		
 		return newJobs;
 	}
 	
 	public static List convertDirectoryToSingleVideos(DirectoryInfo directoryInfo, ConverterOptions converterOptions) {
 		List newJobs = new ArrayList();
 		
 		String[] directory = new File(directoryInfo.getInputDirectory()).list(new VideoFileFilter());
 		
 		for (int i = 0; i < directory.length; i++)
 			if (new File(directoryInfo.getInputDirectory() + File.separator + directory[i]).isDirectory() && directoryInfo.getConvertSubdirectories())
 				newJobs.addAll(convertDirectoryToSingleVideos(new DirectoryAdapter(directoryInfo.getInputDirectory() + File.separator + directory[i], directoryInfo.getOutputDirectory() + File.separator + directory[i], directoryInfo.getConvertSubdirectories()), converterOptions));
 			else if (new File(directoryInfo.getInputDirectory() + File.separator + directory[i]).isFile())
 				newJobs.add(new SingleVideoAdapter(directoryInfo.getInputDirectory() + File.separator + directory[i], directoryInfo.getOutputDirectory() + File.separator + directory[i].substring(0, directory[i].lastIndexOf('.')) + "." + converterOptions.getCurrentProfile().getProfileName() + ".avi"));
 			
 		return newJobs;
 	}
 	
 	public static List separateDVDChaptersToSingleDVDJobs(DVDInfo dvdInfo) {
 		List newJobs = new ArrayList();
 	
 		Chapters[] chapters = dvdInfo.getChapters();
 		
 		if (chapters == null)
 			newJobs.add(dvdInfo);
 		else
 			for (int i = 0; i < chapters.length; i++) {
 				String outputVideo = "";
 				if (chapters[i].getFirstChapter() == chapters[i].getLastChapter())
 					outputVideo = dvdInfo.getOutputVideo().substring(0, dvdInfo.getOutputVideo().lastIndexOf('.')) + ".ch" + chapters[i].getFirstChapterPadded() + ".avi";
 				else
 					outputVideo = dvdInfo.getOutputVideo().substring(0, dvdInfo.getOutputVideo().lastIndexOf('.')) + ".ch" + chapters[i].getFirstChapterPadded() + "-" + chapters[i].getLastChapterPadded() + ".avi";
 				
 				newJobs.add(new DVDAdapter(dvdInfo.getDrive(), dvdInfo.getTitle(), new Chapters[]{chapters[i]}, dvdInfo.getAudioStream(), dvdInfo.getSubtitles(), outputVideo));
 			}
 		
 		return newJobs;
 	}
 	
 	public static List separateMultipleSplitJobsToOneSplitJob(ManualSplitInfo manualSplitInfo) {
 		List newJobs = new ArrayList();
 		
 		for (int i = 0; (i + 1) < manualSplitInfo.getMarks().length; i++)
 			newJobs.add(new ManualSplitAdapter(manualSplitInfo.getVideo(), new Mark[]{manualSplitInfo.getMarks()[i], manualSplitInfo.getMarks()[i + 1]}, i + 1));
 		
 		return newJobs;		
 	}
 	
 	public static List checkForOverwritingFiles(List jobs) {
 		List newJobs = new ArrayList();
 		
 		for (int i = 0; i < jobs.size(); i++) {
 			if (!(jobs.get(i) instanceof OutputVideoInfo))
 				newJobs.add(jobs.get(i));
 			else if (new File(((OutputVideoInfo) jobs.get(i)).getOutputVideo()).exists()) {
 				if (OverwriteDialog.overwriteFile(((OutputVideoInfo) jobs.get(i)).getOutputVideo()))
 					newJobs.add(jobs.get(i));
 			} else
 				newJobs.add(jobs.get(i));
 		}
 		
 		return newJobs;
 	}
 	
 	public void run() {
 		for (int i = 0; i < jobs.size() && !isCanceled; i++) {
 			progressDialogInfo.setCurrentJob(i + 1);
 			
 			if (jobs.get(i) instanceof SingleVideoInfo)
 				convertSingleVideo((SingleVideoInfo) jobs.get(i));;
 			if (jobs.get(i) instanceof DVDInfo)
 				convertDVD((DVDInfo) jobs.get(i));
 			if (jobs.get(i) instanceof ManualSplitInfo)
 				manuallySplitVideo((ManualSplitInfo) jobs.get(i));
 			if (jobs.get(i) instanceof JoinVideosInfo)
 				joinVideos((JoinVideosInfo) jobs.get(i));
 		}
 		
 		if (!isCanceled)
 			progressDialogInfo.complete(exitCode == 0);
 	}
 	
 	public void cancel() {
 		isCanceled = true;
 		
 		if (proc != null)
 			proc.destroy();
 	}
 	
 	private List prepareBaseCommandList(String inputVideo, String outputVideo, MPlayerInfo info) {		
 		int scaledWidth = converterOptions.getDimensions().getWidth();
 		int scaledHeight = (info.getDimensions().getHeight() * converterOptions.getDimensions().getWidth()) / info.getDimensions().getWidth();
 		
 		if (scaledHeight > converterOptions.getDimensions().getHeight()) {
 			scaledWidth = (scaledWidth * converterOptions.getDimensions().getHeight()) / scaledHeight;
 			scaledHeight = converterOptions.getDimensions().getHeight();
 		}
 		
 		double ofps = (info.getFrameRate() > converterOptions.getCurrentProfile().getMaxFrameRate() ? converterOptions.getCurrentProfile().getMaxFrameRate() : info.getFrameRate());
 		
 		String vf = "filmdint=io=" + ((int) Math.round(info.getFrameRate() * 1000)) + ":" + ((int) Math.round(ofps * 1000));
 		if (converterOptions.getPanAndScan())
 			vf += ",scale=" + ((int) ((info.getDimensions().getWidth()) * (((double) converterOptions.getDimensions().getHeight()) / (double) info.getDimensions().getHeight()))) + ":" + converterOptions.getDimensions().getHeight() + ",crop=" + converterOptions.getDimensions().getWidth() + ":" + converterOptions.getDimensions().getHeight();
 		else
 			vf += ",scale=" + scaledWidth + ":" + scaledHeight + ",expand=" + converterOptions.getDimensions().getWidth() + ":" + converterOptions.getDimensions().getHeight();
 		vf += ",harddup";
 		
 		String af = "";
 		if (converterOptions.getVolumeFilter() == VolumeFilter.VOLNORM)
 			af = "volnorm," + af;
 		if (converterOptions.getVolumeFilter() == VolumeFilter.VOLUME)
 			af = "volume=" + converterOptions.getGain() + "," + af;
 		
 		List commandList = new ArrayList();
 		
 		commandList.add(MPlayerInfo.getMPlayerPath() + "mencoder");
 		commandList.add(inputVideo);
 		
 		if (converterOptions.getCurrentProfile().getWrapperFormat().equals("mp4")) {
 			commandList.add("-of");
 			commandList.add("lavf");
 			commandList.add("-lavfopts");
 			commandList.add("format=mp4:i_certify_that_my_video_stream_does_not_use_b_frames");
 		}
 		
 		commandList.add("-o");
 		commandList.add(outputVideo);
 		commandList.add("-ovc");
 		commandList.add("xvid");
 		commandList.add("-xvidencopts");
 		commandList.add("bitrate=" + converterOptions.getVideoBitrate() + ":max_bframes=0");
 		commandList.add("-oac");
 		if (converterOptions.getCurrentProfile().getAudioFormat().equals("aac")) {
 			commandList.add("faac");
 			commandList.add("-faacopts");
 			commandList.add("br=" + converterOptions.getAudioBitrate());
 		} else {
 			commandList.add("mp3lame");
 			commandList.add("-lameopts");
 			commandList.add("mode=2:cbr:br=" + converterOptions.getAudioBitrate());
 		}
 		commandList.add("-vf");
 		commandList.add(vf);
 		
 		if (!af.equals("")) {
 			commandList.add("-af");
 			commandList.add(af);
 		}
 		
 		commandList.add("-ofps");
 		commandList.add("" + ofps);
 		commandList.add("-srate");
 		commandList.add("44100");
 		
 		if (!converterOptions.getAutoSync()) {
 			commandList.add("-mc");
 			commandList.add("0");
 			
 			int offset = converterOptions.getAudioDelay();
 			commandList.add("-delay");
 			commandList.add("" + (offset / 1000.0));
 		}
 		
 		return commandList;
 	}
 	
 	public void convertSingleVideo(SingleVideoInfo singleVideoInfo) {
 		progressDialogInfo.setInputVideo(new File(singleVideoInfo.getInputVideo()).getName());
 		progressDialogInfo.setOutputVideo(new File(singleVideoInfo.getInputVideo()).getName());
 		progressDialogInfo.setStatus("Gathering information about the input video...");
 		
 		MPlayerInfo info = new MPlayerInfo(singleVideoInfo.getInputVideo());
 		
 		List commandList = prepareBaseCommandList(singleVideoInfo.getInputVideo(), singleVideoInfo.getOutputVideo(), info);
 		
 		String[] command = new String[commandList.size()];
 		for (int i = 0; i < command.length; i++)
 			command[i] = (String) commandList.get(i);
 		
 		if (!isCanceled) {
 			new File(singleVideoInfo.getOutputVideo()).getParentFile().mkdirs();
 			progressDialogInfo.setStatus("Converting");
 			splitVideo(singleVideoInfo.getOutputVideo(), runConversionCommand(command));
 		}
 	}
 	
 	public void convertDVD(DVDInfo dvdInfo) {
 		String inputVideo = "Title " + dvdInfo.getTitle() + " of the DVD at " + dvdInfo.getDrive();
 		
 		Chapters[] chapters = dvdInfo.getChapters();
 		if (chapters != null) {
 			if (chapters[0].getFirstChapter() == chapters[0].getLastChapter())
 				inputVideo = "Chapter " + chapters[0].getFirstChapter() + " of " + inputVideo;
 			else 
 				inputVideo = "Chapters " + chapters[0].getFirstChapter() + "-" + chapters[0].getLastChapter() + " of " + inputVideo;
 		}
 		
 		progressDialogInfo.setInputVideo(inputVideo);
 		progressDialogInfo.setOutputVideo(new File(dvdInfo.getOutputVideo()).getName());
 		progressDialogInfo.setStatus("Gathering information about the input video...");
 		
 		MPlayerInfo info = new MPlayerInfo("dvd://" + dvdInfo.getTitle(), dvdInfo.getDrive());
 		
 		List commandList = prepareBaseCommandList("dvd://" + dvdInfo.getTitle(), dvdInfo.getOutputVideo(), info);
 		
 		commandList.add("-dvd-device");
 		commandList.add(dvdInfo.getDrive());
 	
 		if (dvdInfo.getAudioStream() > -1) {
 			commandList.add("-aid");
 			commandList.add("" + dvdInfo.getAudioStream());
 		}
 		
 		if (dvdInfo.getSubtitles() > -1) {
 			commandList.add("-sid");
 			commandList.add("" + dvdInfo.getSubtitles());
 		}
 		
 		if (dvdInfo.getChapters() != null) {
 			commandList.add("-chapter");
 			commandList.add(dvdInfo.getChapters()[0].getFirstChapter() + "-" + dvdInfo.getChapters()[0].getLastChapter());
 		}
 		
 		String[] command = new String[commandList.size()];
 		for (int i = 0; i < command.length; i++)
 			command[i] = (String) commandList.get(i);
 		
 		if (!isCanceled) {
 			new File(dvdInfo.getOutputVideo()).getParentFile().mkdirs();
 			progressDialogInfo.setStatus("Converting");
 			splitVideo(dvdInfo.getOutputVideo(), runConversionCommand(command));
 		}
 	}
 	
 	public void manuallySplitVideo(ManualSplitInfo manualSplitInfo) {
 		String outputVideo = manualSplitInfo.getVideo().substring(0, manualSplitInfo.getVideo().lastIndexOf('.')) + ".part" + manualSplitInfo.getPart() + ".avi";
 		
 		progressDialogInfo.setInputVideo(manualSplitInfo.getVideo());
 		progressDialogInfo.setOutputVideo(outputVideo);
 		progressDialogInfo.setStatus("Splitting");
 		
 		if (manualSplitInfo.getMarks()[0].getTime() == Mark.START_MARK)
 			runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", manualSplitInfo.getVideo(), "-o", outputVideo, "-ovc", "copy", "-oac", "copy", "-endpos", "" + manualSplitInfo.getMarks()[1].getTime()});
 		else if (manualSplitInfo.getMarks()[1].getTime() == Mark.END_MARK)
 			runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", manualSplitInfo.getVideo(), "-o", outputVideo, "-ovc", "copy", "-oac", "copy", "-ss", "" + manualSplitInfo.getMarks()[0].getTime()});
 		else
 			runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", manualSplitInfo.getVideo(), "-o", outputVideo, "-ovc", "copy", "-oac", "copy", "-ss", "" + manualSplitInfo.getMarks()[0].getTime(), "-endpos", "" + (manualSplitInfo.getMarks()[1].getTime() - manualSplitInfo.getMarks()[0].getTime())});
 	}
 	
 	public void joinVideos(JoinVideosInfo joinVideosInfo) {
 		try {
 			String[] inputVideos = joinVideosInfo.getInputVideos();
 			/* String[] tempVideos = new String[inputVideos.length];
 			
 			for (int i = 0; i < inputVideos.length; i++) {
 				File tempFile = File.createTempFile("iriverter-", ".avi");
 				tempFile.deleteOnExit();
 				
 				progressDialogInfo.setInputVideo(new File(inputVideos[i]).getName());
 				progressDialogInfo.setOutputVideo(tempFile.getName());
 				progressDialogInfo.setStatus("Fixing header");
 				
 				runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", "-idx", inputVideos[i], "-o", tempFile.toString(), "-ovc", "copy", "-oac", "copy"});
 				
 				tempVideos[i] = tempFile.toString();
 			} */
 			
 			File tempFile = File.createTempFile("iriverter-", ".avi");
 			tempFile.deleteOnExit();
 			
 			progressDialogInfo.setOutputVideo(tempFile.getName());
 			progressDialogInfo.setStatus("Concatenating videos to a temporary file...");
 			
 			FileOutputStream out = new FileOutputStream(tempFile);
 			// SequenceInputStream in = new SequenceInputStream(new ListOfFiles(tempVideos, progressDialogInfo));
 			SequenceInputStream in = new SequenceInputStream(new ListOfFiles(inputVideos, progressDialogInfo));
 			byte[] bytes = new byte[4096];
 			int length;
 			
 			while ((length = in.read(bytes)) != -1 && !isCanceled)
 				out.write(bytes, 0, length);
 			
 			progressDialogInfo.setPercentComplete(100);
 			
 			if (!isCanceled) {
 				progressDialogInfo.setInputVideo(tempFile.getName());
 				progressDialogInfo.setOutputVideo(new File(joinVideosInfo.getOutputVideo()).getName());
 				progressDialogInfo.setStatus("Writing header");
 				splitVideo(joinVideosInfo.getOutputVideo(), runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", "-forceidx", tempFile.toString(), "-o", joinVideosInfo.getOutputVideo(), "-ovc", "copy", "-oac", "copy"}));
 			}
 		} catch (IOException e) {
 			// empty
 		}
 	}
 	
 	public int runConversionCommand(String[] command) {
 		MencoderStreamParser inputStream = null;
 		MencoderStreamParser errorStream = null;
 		
 		System.out.print("DEBUG--- Running: ");
 		for (int i = 0; i < command.length; i++)
 			System.out.print(command[i] + " ");
 		System.out.println();
 		
 		try {
 			proc = Runtime.getRuntime().exec(command);
 			
 			inputStream = new MencoderStreamParser(progressDialogInfo);
 			inputStream.parse(new BufferedReader(new InputStreamReader(proc.getInputStream())));
 			errorStream = new MencoderStreamParser(progressDialogInfo);
 			errorStream.parse(new BufferedReader(new InputStreamReader(proc.getErrorStream())));
 			
 			exitCode = proc.waitFor();
 			proc = null;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if (exitCode != 0)
 			isCanceled = true;
 		
 		return isCanceled ? 0 : ((errorStream.getLength() > -1) ? errorStream.getLength() : inputStream.getLength());
 	}
 	
 	public void splitVideo(String inputVideo, int length) {
 		if (length < converterOptions.getSplitTime() * 60)
 			return;
 		
 		if (!converterOptions.getAutoSplit()) {
 			notSplitVideos.add(inputVideo);
 			return;
 		}
 		
 		int pieces = (length / (converterOptions.getSplitTime() * 60)) + 1;
 		for (int i = 0; i < pieces; i++) {
 			String outputVideo = inputVideo.substring(0, inputVideo.lastIndexOf('.')) + ".part" + (i + 1) + ".avi";
 			
 			progressDialogInfo.setInputVideo(new File(inputVideo).getName());
 			progressDialogInfo.setOutputVideo(new File(outputVideo).getName());
 			progressDialogInfo.setStatus("Splitting");
 			
 			if ((i + 1) == 1)
 				runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", inputVideo, "-o", outputVideo, "-ovc", "copy", "-oac", "copy", "-endpos", "" + (length / pieces)});
 			else if ((i + 1) == pieces)
 				runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", inputVideo, "-o", outputVideo, "-ovc", "copy", "-oac", "copy", "-ss", "" + (length / pieces) * i});
 			else
 				runConversionCommand(new String[]{MPlayerInfo.getMPlayerPath() + "mencoder", inputVideo, "-o", outputVideo, "-ovc", "copy", "-oac", "copy", "-ss", "" + (length / pieces) * i, "-endpos", "" + (length / pieces)});
 		}
 	}
 	
 	public List getNotSplitVideos() {
 		return notSplitVideos;
 	}
 }
