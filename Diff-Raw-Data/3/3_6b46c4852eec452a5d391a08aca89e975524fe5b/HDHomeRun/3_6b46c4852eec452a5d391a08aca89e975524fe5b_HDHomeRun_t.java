 package com.koehn.hdhomerun;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.Collator;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.List;
 import java.util.TreeMap;
 
 public class HDHomeRun {
 
 	public static void main(String args[]) throws IOException {
 		int tuner = 1;
 		ChannelInfoFetcher channelInfoFetcher = new ChannelInfoFetcher();
 		List<String> deviceIds = ConfigRunner.discover();
 		for (String deviceId : deviceIds) {
 			List<Channel> channels = channelInfoFetcher.getChannels(deviceId,
 					tuner);
 
 			TreeMap<Integer, Channel> channelsByUserChannelNumber = new TreeMap<>();
 			TreeMap<String, Channel> channelsByLabel = new TreeMap<>(
 					Collator.getInstance());
 
 			for (Channel channel : channels) {
 				channelsByUserChannelNumber.put(channel.getUserChannel(),
 						channel);
 				channelsByLabel.put(channel.getLabel(), channel);
 			}
 
 			File baseDir = new File("Live TV");
 			if (!baseDir.exists()) {
 				if (!baseDir.mkdir()) {
 					throw new IllegalStateException(
 							"Unable to create base directory.");
 				}
 			}
 
 			File channelDir = new File(baseDir, "By Number");
 			if (!channelDir.exists()) {
 				if (!channelDir.mkdir()) {
 					throw new IllegalStateException(
 							"Unable to create Channel directory.");
 				}
 			}
 
 			NumberFormat channelFormat = new DecimalFormat("000");
 			for (Channel channel : channelsByUserChannelNumber.values()) {
 				String filename = channelFormat
 						.format(channel.getUserChannel())
 						+ " - "
 						+ channel.getLabel() + ".strm";
 				File file = new File(channelDir, filename);
 				createStrmFile(deviceId, tuner, channel, file);
 			}
 
 			File labelDir = new File(baseDir, "By Label");
 			if (!labelDir.exists()) {
 				if (!labelDir.mkdir()) {
 					throw new IllegalStateException(
 							"Unable to create Label directory.");
 				}
 			}
 			for (Channel channel : channelsByLabel.values()) {
 				String filename = channel.getLabel() + " - "
 						+ channel.getUserChannel() + ".strm";
 				File file = new File(labelDir, filename);
 				createStrmFile(deviceId, tuner, channel, file);
 			}
 		}
 	}
 
 	private static void createStrmFile(String deviceId, int tuner,
 			Channel channel, File file) throws IOException,
 			FileNotFoundException {
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
 				new FileOutputStream(file)));
		writer.write("hdhomerun://" + deviceId + "-" + tuner + "/tuner" + tuner
 				+ "?channel=auto:" + channel.getChannel() + "&program="
 				+ channel.getProgram());
 		writer.close();
 	}
 }
