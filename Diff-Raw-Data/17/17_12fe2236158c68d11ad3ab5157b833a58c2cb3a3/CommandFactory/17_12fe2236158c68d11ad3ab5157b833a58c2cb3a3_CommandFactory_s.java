 /**
  * 
  */
 package shtaag.network.curl.impl;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import shtaag.network.curl.framework.OutputWriter;
 import shtaag.network.curl.impl.option.Option;
 import shtaag.network.curl.impl.option.OptionEntity;
 import shtaag.network.curl.impl.output.FileOutputWriter;
 import shtaag.network.curl.impl.output.StandardOutWriter;
 
 /**
  * @author takei_s
  * @date 2011/12/25
  */
 public class CommandFactory {
 
 	public Command getCommand(List<OptionEntity> optionList, List<URL> urlList) {
 
 		Map<String, String> headers = getHeaders(optionList);
 		String command = command(optionList);
 		List<File> files = getFileList(optionList);
 		Queue<UrlFileHandling> urlFilehandling = combineUrlFile(urlList, files);
 		OutputWriter writer = getWriter(files);
 		boolean isVerbose = isVerbose(optionList);
 		boolean proxy = proxy(optionList);
 		return new Command(headers, command, urlFilehandling, writer, isVerbose, proxy);
 	}
 
 
 	/**
 	 * @param files
 	 * @return
 	 */
 	private OutputWriter getWriter(List<File> files) {
 		if (files.isEmpty()) {
 			return new StandardOutWriter();
 		}
 		return new FileOutputWriter();
 	}
 
 
 	/**
 	 * @param urlList
 	 * @param files
 	 * @return
 	 */
 	private Queue<UrlFileHandling> combineUrlFile(List<URL> urlList,
 			List<File> files) {
 		
 		Queue<UrlFileHandling> result = new ArrayDeque<UrlFileHandling>();
 		for (int i = 0; i < urlList.size(); i++) {
 			if (files.isEmpty()) {
				result.add(new UrlFileHandling(urlList.get(i), new UrlFileHandling.StdOut(null)));
 			} else {
 				result.add(new UrlFileHandling(urlList.get(i), files.get(i)));
 			}
 		}
 		return result;
 	}
 
 
 	/**
 	 * @param optionList
 	 * @return
 	 */
 	private boolean proxy(List<OptionEntity> optionList) {
 		// TODO 複数対応
 		for (OptionEntity option : optionList) {
 			if (option.type.equals(Option.PROXYHOST)) {
 				return Boolean.valueOf(option.value);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param optionList
 	 * @return
 	 */
 	private boolean isVerbose(List<OptionEntity> optionList) {
 		// TODO 複数対応˙
 		for (OptionEntity option : optionList) {
 			if (option.type.equals(Option.VERBOSE)) {
 				return Boolean.valueOf(option.value);
 			}
 		}
 		return false;
 	}
 
 	private Map<String, String> getHeaders(List<OptionEntity> optionList) {
 		Map<String, String> result = new HashMap<String, String>();
 		for (OptionEntity option : optionList) {
 			if (option.type.equals(Option.HEADER)) {
 				String[] keyValuePair = parseHeader(option.value);
 				result.put(keyValuePair[0], keyValuePair[1]);
 			}
 		}
 		return result;
 	}
 	private String[] parseHeader(String str) {
 		String[] result = str.split(":");
 		if (result.length > 2) {
 			throw new IllegalArgumentException("Illegal Header Value: " + str);
 		}
 		return result;
 	}
 	
 	private String command(List<OptionEntity> optionList) {
 		// TODO 二つ以上来るとあと勝ちになってる
 		String command = null;
 		for (OptionEntity option : optionList) {
 			if (option.type.equals(Option.COMMAND)) {
 				command = option.value;
 			}
 		}
 		return command;
 	}
 	
 	private List<File> getFileList(List<OptionEntity> optionList) {
 		List<File> result = new ArrayList<File>();
 		for (OptionEntity option : optionList) {
 			if (option.type.equals(Option.FILE)) {
 				result.add(new File(option.value));
 			}
 		}
 		if (result.isEmpty()) {
 			return Collections.emptyList();
 		}
 		return result;
 	}
 
 }
