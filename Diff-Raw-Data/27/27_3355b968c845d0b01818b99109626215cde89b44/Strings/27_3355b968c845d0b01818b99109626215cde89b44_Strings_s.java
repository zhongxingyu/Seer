 package neo.java.commons;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.security.MessageDigest;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * 字符串工具类
  * 
  * @author neo
  */
 public class Strings {
 
 	/** UTF-8 字符集的标识 */
 	public static final String UTF_8 = "UTF-8";
 	/** ISO-8859-1 字符集的标识 */
 	public static final String ISO8859 = "ISO-8859-1";
 	/** 检查 URL 的正则 */
 	private static final String URL_CHECKER = "^(http|ftp)?(s)?://([\\w-]+.)+[\\w-]+(/[\\w- ./?%&=+-_]*)?$";
 
 	/**
 	 * 字符串判空
 	 * 
 	 * @param string
 	 *            待判断的字符串
 	 * @return 是否为空
 	 */
 	public static boolean isEmpty(String string) {
 		if (null == string || 0 == string.trim().length()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * 是否为有效的 URL 地址
 	 * 
 	 * @param url
 	 *            待判断的 URL 地址
 	 * @return 是否
 	 */
 	public static boolean isNetworkURL(String url) {
		if (Pattern.compile(URL_CHECKER, Pattern.CASE_INSENSITIVE).matcher(url)
				.find()) {
			return true;
		} else {
			return false;
		}
 	}
 
 	/**
 	 * 控制台遍历输出 Map<String, String>
 	 * 
 	 * @param map
 	 *            待遍历的 Map
 	 */
 	public static void sysoutMaps(Map<String, String> map) {
 		String key = null;
 		StringBuilder stringBuilder = new StringBuilder();
 		Iterator<String> iterator = map.keySet().iterator();
 		while (iterator.hasNext()) {
 			key = iterator.next();
 			stringBuilder.append(key + " => " + map.get(key) + ", ");
 		}
 
 		System.out.println(stringBuilder.toString() + "\n");
 	}
 
 	/**
 	 * 获取当前时间，格式自定
 	 * 
 	 * @param pattern
 	 *            简单日期格式模式
 	 * @return 格式化后的字符串对象
 	 */
 	public static String getCurrentTimeString(String pattern) {
 		SimpleDateFormat format = new SimpleDateFormat(pattern);
 		return format.format(new Date());
 	}
 
 	/**
 	 * 从时间戳转指定格式化日期
 	 * 
 	 * @param timeStamp
 	 *            时间戳
 	 * @param pattern
 	 *            简单日期格式模式
 	 * @return 格式化后的字符串对象
 	 */
 	public static String getFormattedTimeString(int timeStamp, String pattern) {
 		SimpleDateFormat format = new SimpleDateFormat(pattern);
 		format.setTimeZone(TimeZone.getTimeZone("UTC"));
 		return format.format(timeStamp * 1000);
 	}
 
 	/**
 	 * 当前字符集转 ISO8859
 	 * 
 	 * @param string
 	 *            待转换的字符串
 	 * @return 转换后的字符串
 	 * @throws UnsupportedEncodingException
 	 */
 	public static String getISO(String string)
 			throws UnsupportedEncodingException {
 		return new String(string.getBytes(), ISO8859);
 	}
 
 	/**
 	 * 指定字符集的字符串转换成 ISO8859
 	 * 
 	 * @param string
 	 *            待转换的字符串
 	 * @param charset
 	 *            字符集标识
 	 * @return 转换后的字符串
 	 * @throws UnsupportedEncodingException
 	 */
 	public static String getISO(String string, String charset)
 			throws UnsupportedEncodingException {
 		return new String(string.getBytes(charset), ISO8859);
 	}
 
 	/**
 	 * 简单的正则
 	 * 
 	 * @param reg
 	 *            正则模式字符串
 	 * @param content
 	 *            待匹配的内容
 	 * @return 匹配结果集对象
 	 */
 	public static Matcher grep(String reg, String content) {
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(content);
		return matcher;
 	}
 
 	/**
 	 * 将字节数组的十六进制转换成字符串
 	 * 
 	 * @param bytes
 	 *            字节数组
 	 * @return 十六进制结果
 	 */
 	public static String fromBytes(byte[] bytes) {
 		if (null != bytes) {
 			StringBuilder sBuilder = new StringBuilder();
 			for (int i = 0; i < bytes.length; i++) {
 				sBuilder.append(String.format("%02X", bytes[i]));
 			}
 			return sBuilder.toString();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * 对字符串使用特定算法的校验，比如 MD5 SHA-1
 	 * 
 	 * @param content
 	 *            字符串内容
 	 * @param algorithm
 	 *            校验算法
 	 * @return 校验结果
 	 */
 	public static String digestString(String content, String algorithm) {
 		if (isEmpty(content)) {
 			return null;
 		}
 
 		MessageDigest digest = null;
 
 		try {
 			digest = MessageDigest.getInstance(algorithm);
 			digest.reset();
 			digest.update(content.getBytes());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		if (null != digest) {
 			return fromBytes(digest.digest());
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * 通过指定算法校验文件
 	 * 
 	 * @param fileName
 	 *            文件名
 	 * @param algorithm
 	 *            校验算法，如 MD5 SHA-1
 	 * @return 校验值
 	 * @throws IOException
 	 */
 	public static String digestFile(String fileName, String algorithm)
 			throws IOException {
 		if (isEmpty(fileName)) {
 			return null;
 		}
 
 		MessageDigest digest = null;
 		FileInputStream inputStream = null;
 
 		try {
 			File file = new File(fileName);
 			inputStream = new FileInputStream(file);
 			FileChannel channel = inputStream.getChannel();
 			MappedByteBuffer byteBuffer = channel.map(
 					FileChannel.MapMode.READ_ONLY, 0, file.length());
 			inputStream.close();
 			inputStream = null;
 
 			digest = MessageDigest.getInstance(algorithm);
 			digest.reset();
 			digest.update(byteBuffer);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		if (null != inputStream) {
 			inputStream.close();
 		}
 
 		if (null != digest) {
 			return fromBytes(digest.digest());
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * 执行某个 shell 命令，可区分当前的平台
 	 * 
 	 * @param cmd
 	 *            命令字符串
 	 * @return 返回执行的结果，阻塞的
 	 */
 	public static String execShell(String cmd) {
 		String[] cmds = null;
 
 		if (System.getProperty("os.name").contains("ows")) {
 			cmds = new String[] { "cmd.exe", "/c", cmd };
 		} else {
 			cmds = new String[] { "sh", "-c", cmd };
 		}
 
 		final int readBuffer = 5555;
 		StringBuilder sBuilder = new StringBuilder();
 
 		try {
 			Process process = Runtime.getRuntime().exec(cmds);
 			BufferedReader stdout = new BufferedReader(new InputStreamReader(
 					process.getInputStream()), readBuffer);
 			BufferedReader stderr = new BufferedReader(new InputStreamReader(
 					process.getErrorStream()), readBuffer);
 
 			String perLine = null;
 
 			while ((null != (perLine = stdout.readLine()))
 					|| (null != (perLine = stderr.readLine()))) {
 				if (false == isEmpty(perLine)) {
 					sBuilder.append(perLine + "\n");
 				}
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return sBuilder.toString();
 	}
 
 	/**
 	 * 判断两个版本字符串信息是否为新版本
 	 * 
 	 * @param installedVersion
 	 *            当前的版本字符串
 	 * @param getVersion
 	 *            待比较的版本字符串
 	 * @return 是否为新版本
 	 */
 	public static boolean isNewerVersion(String installedVersion,
 			String getVersion) {
 
 		if (installedVersion.equals(getVersion))
 			return false;
 
 		int[] installeds = versionString2Ints(installedVersion);
 		int[] gets = versionString2Ints(getVersion);
 
 		if (gets[0] > installeds[0])
 			return true;
 		else if (gets[0] == installeds[0] && gets[1] > installeds[1])
 			return true;
 		else if (gets[0] == installeds[0] && gets[1] == installeds[1]
 				&& gets[2] > installeds[2])
 			return true;
 		else if (gets[0] == installeds[0] && gets[1] == installeds[1]
 				&& gets[2] == installeds[2] && gets[3] > installeds[3])
 			return true;
 		else if (gets[0] == installeds[0] && gets[1] == installeds[1]
 				&& gets[2] == installeds[2] && gets[3] == installeds[3]
 				&& gets[4] > installeds[4])
 			return true;
 		else if (gets[0] == installeds[0] && gets[1] == installeds[1]
 				&& gets[2] == installeds[2] && gets[3] == installeds[3]
 				&& gets[4] == installeds[4] && gets[5] > installeds[5])
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * 稍微智能一点的 String 转 int
 	 * 
 	 * @param string
 	 *            待转换的字符串
 	 * @return 转换后的整型变量
 	 */
 	private static int getIntFromString(String string) {
 		int result = 0;
 
 		try {
 			result = Integer.parseInt(string);
 		} catch (NumberFormatException e) {
 			int endIndex = 0;
 
 			for (int i = 0; i < string.length(); i++) {
 				if (0x30 <= string.charAt(i) && 0x39 >= string.charAt(i)) {
 					endIndex++;
 				} else {
 					break;
 				}
 			}
 
 			if (0 != endIndex) {
 				result = Integer.parseInt(string.substring(0, endIndex));
 			} else {
 				result = 0;
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * 版本信息字符串转整型数组
 	 * 
 	 * @param version
 	 *            版本信息字符串
 	 * @return 整型数组
 	 */
 	private static int[] versionString2Ints(String version) {
 		int[] vers = new int[] { 0, 0, 0, 0, 0, 0 };
 
 		int start = 0;
 		int end = version.indexOf(".");
 
 		if (end > start) {
 			vers[0] = getIntFromString(version.substring(start, end));
 
 			start = end + 1;
 			end = version.indexOf(".", start);
 
 			if (end > start) {
 				vers[1] = getIntFromString(version.substring(start, end));
 
 				start = end + 1;
 				end = version.indexOf(".", start);
 
 				if (end > start) {
 					vers[2] = getIntFromString(version.substring(start, end));
 
 					start = end + 1;
 					end = version.indexOf(".", start);
 
 					if (end > start) {
 						vers[3] = getIntFromString(version
 								.substring(start, end));
 
 						start = end + 1;
 						end = version.indexOf(".", start);
 
 						if (end > start) {
 							vers[4] = getIntFromString(version.substring(start,
 									end));
 
 							start = end + 1;
 
 							if (version.length() > start)
 								vers[5] = getIntFromString(version
 										.substring(start));
 						} else {
 							if (version.length() > start)
 								vers[4] = getIntFromString(version
 										.substring(start));
 						}
 					} else {
 						if (version.length() > start)
 							vers[3] = getIntFromString(version.substring(start));
 					}
 
 				} else {
 					if (version.length() > start)
 						vers[2] = getIntFromString(version.substring(start));
 				}
 			} else {
 				if (version.length() > start)
 					vers[1] = getIntFromString(version.substring(start));
 			}
 		} else {
 			if (version.length() > start)
 				vers[0] = getIntFromString(version.substring(start));
 		}
 
 		return vers;
 	}
 }
