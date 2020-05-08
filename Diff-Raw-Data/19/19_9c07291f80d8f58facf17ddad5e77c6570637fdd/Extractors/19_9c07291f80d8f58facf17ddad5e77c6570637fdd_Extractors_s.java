 package org.gridkit.data.extractors.common;
 
 import java.nio.ByteBuffer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Extractors {
 
 	@SuppressWarnings("unchecked")
 	public static <V> V extract(ByteBuffer binary, BinaryExtractor<V> extractor) {
 		BinaryExtractorSet set = extractor.newExtractorSet();
 		final int id = set.addExtractor(extractor);
 		set.compile();
 		final Object[] result = new Object[1];
 		set.extractAll(binary, new ResultVectorReceiver() {
 			@Override
 			public void push(int pid, Object part) {
 				if (id == pid) {
 					result[0] = part;
 				}
 				else {
 					throw new IllegalArgumentException("Unknown argiment ID: " + pid);
 				}
 				
 			}
 		});
 		return (V) result[0];
 	}	
 
 	public static Object[] extractAll(ByteBuffer binary, BinaryExtractor<?>... extractors) {
 		CompositeExtractorSet set = new CompositeExtractorSet();
 		final int[] idmap = new int[extractors.length];
 		for(int i = 0; i != idmap.length; ++i) {
 			idmap[i] = set.addExtractor(extractors[i]);
 		}
 		
 		final Object[] result = new Object[idmap.length];
 		set.extractAll(binary, new ResultVectorReceiver() {
 			@Override
 			public void push(int pid, Object part) {
 				for(int i = 0; i != idmap.length; ++i) {
 					if (idmap[i] == pid) {
 						result[i] = part;
 					}
 				}
 			}
 		});
 		return result;
 	}	
 	
	public static String dump(BinaryExtractorSet set, int ident) {
 		StringBuilder sb = new StringBuilder();
 		set.dump(sb);
		if (ident == 0) {
 			return sb.toString();
 		}
 		else {
			return ident(sb.toString(), ident);
 		}
 	}
 
	private static String ident(String string, int ident) {
 		StringBuilder sb = new StringBuilder();
 		
 		Pattern regex = Pattern.compile("(\n)|(<[^/>]+>)|(<[/][^/>]+>)|(<[^/>]+[/]>)");
 		Matcher m = regex.matcher(string);
 		int ci = 0;
 		int p = 0;
 		while(true) {
 			if (m.find(p)) {
 				if (m.start() > p) {
 					sb.append(string.substring(p, m.start()));
 				}				
 				if ("\n".equals(m.group())) {
 					sb.append("\n");
					for(int i = 0; i < ci * ident; ++i) {
 						sb.append(' ');
 					}
 				}
 				else if (m.group().endsWith("/>")) {
 					sb.append(m.group());
 				}
 				else if (m.group().startsWith("</")) {
 					--ci;
					// undo identation before closing tag
					for(int i = 0; i < ident; ++i) {
 						if (sb.charAt(sb.length() - 1) == ' ') {
 							sb.setLength(sb.length() - 1);
 						}
 					}
 					sb.append(m.group());
 				}
 				else {
 					++ci;
 					sb.append(m.group());
 				}
 				p = m.end();
 			}
 			else {
 				sb.append(string.substring(p, string.length()));
 				break;
 			}
 		}
 		
 		return sb.toString();
 	}
 }
