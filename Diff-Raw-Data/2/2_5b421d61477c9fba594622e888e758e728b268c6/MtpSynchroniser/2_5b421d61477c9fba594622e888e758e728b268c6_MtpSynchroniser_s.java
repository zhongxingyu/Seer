 package uk.co.unclealex.music.sync;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import uk.co.unclealex.music.MtpDevice;
 
 public class MtpSynchroniser extends AbstractFileSystemSynchroniser<MtpDevice> {
 
 	private Pattern i_deviceFilePattern;
 	private File i_mountPoint;
 	
 	@Override
 	protected void initialiseDevice() throws IOException {
 		setDeviceFilePattern(Pattern.compile("(.+?)\\.([0-9]+)\\." + Pattern.quote(getDevice().getEncoding().getExtension())));
 		File tempDir = new File(System.getProperty("java.io.tmpdir"));
 		File mountPoint;
 		long time = System.currentTimeMillis();
		while (!(mountPoint = new File(tempDir, "sync" + time)).exists()) {
 			time++;
 		}
 		mountPoint.mkdir();
 		setMountPoint(mountPoint);
 		mountPoint.deleteOnExit();
 		setDeviceRoot(new File(mountPoint, "Music"));
 		ProcessBuilder processBuilder = new ProcessBuilder("mtpfs", mountPoint.getAbsolutePath());
 		processBuilder.start();
 	}
 
 	@Override
 	protected DeviceFile createDeviceFile(String relativePath, File f) {
 		String deviceRelativePath;
 		long lastModified;
 		Matcher matcher = getDeviceFilePattern().matcher(relativePath);
 		if (matcher.matches()) {
 			deviceRelativePath = matcher.group(1) + "." + getDevice().getEncoding().getExtension();
 			lastModified = Long.parseLong(matcher.group(2));
 		}
 		else {
 			deviceRelativePath = relativePath;
 			lastModified = 0;
 		}
 		return new DeviceFile(relativePath, deviceRelativePath, lastModified);
 	}
 
 	@Override
 	protected String createRemoteRelativeFilePath(LocalFile localFile) {
 		String extension = "." + getDevice().getEncoding().getExtension();
 		long lastModified = localFile.getLastModified();
 		return localFile.getRelativePath().replace(extension, "." + Long.toString(lastModified) + extension);
 	}
 
 	@Override
 	protected void disconnect() throws IOException {
 		ProcessBuilder processBuilder = new ProcessBuilder("fusermount", "-u", getMountPoint().getAbsolutePath());
 		try {
 			processBuilder.start().waitFor();
 		}
 		catch (InterruptedException e) {
 			throw new IOException(e);
 		}
 		getMountPoint().delete();
 	}
 
 	public Pattern getDeviceFilePattern() {
 		return i_deviceFilePattern;
 	}
 
 	public void setDeviceFilePattern(Pattern deviceFilePattern) {
 		i_deviceFilePattern = deviceFilePattern;
 	}
 
 	public File getMountPoint() {
 		return i_mountPoint;
 	}
 
 	public void setMountPoint(File mountPoint) {
 		i_mountPoint = mountPoint;
 	}
 }
