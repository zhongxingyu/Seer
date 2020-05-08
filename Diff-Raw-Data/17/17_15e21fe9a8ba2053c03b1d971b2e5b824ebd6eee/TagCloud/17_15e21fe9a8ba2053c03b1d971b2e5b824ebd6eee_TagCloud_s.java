 package com.github.diaosi.BDAM.mapreduce;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.TreeMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.github.diaosi.BDAM.inputformat.XmlInputFormat;
 
 public class TagCloud {
 	public static class Map extends MapReduceBase implements
 			Mapper<LongWritable, Text, Text, Text> {
 		private DocumentBuilderFactory dbFactory;
 		private DocumentBuilder dBuilder;
 
 		public Map() throws Exception {
 			super();
 			dbFactory = DocumentBuilderFactory.newInstance();
 			dBuilder = dbFactory.newDocumentBuilder();
 		}
 
 		@Override
 		public void map(LongWritable key, Text value,
 				OutputCollector<Text, Text> output, Reporter reporter)
 				throws IOException {
 			InputSource is = new InputSource(new StringReader(value.toString()));
 			Document doc;
 			try {
 				doc = dBuilder.parse(is);
 				doc.getDocumentElement().normalize();
 
 				NodeList nList = doc.getElementsByTagName("revision");
 				Element nNode = (Element) nList.item(0);
 
 				Text recencyDate = new Text(parseRecencyDate(getTagValue(
 						"timestamp", nNode)));
 
 				NodeList cateList = nNode.getElementsByTagName("category");
 				if (recencyDate.getLength() == 10) {
 					for (int i = 0; i < cateList.getLength(); i++) {
 						Node cate = cateList.item(i);
 						String tmp = getRealCategory(cate.getTextContent()
 								.trim());
 						if (!tmp.equals(""))
 							output.collect(recencyDate, new Text(tmp));
 					}
 				}
 
 			} catch (SAXException e) {
 				e.printStackTrace();
 				System.out.println(value.toString());
 			}
 		}
 
 		private static String getTagValue(String sTag, Element eElement) {
 			NodeList nList = eElement.getElementsByTagName(sTag).item(0)
 					.getChildNodes();
 			Node nValue = nList.item(0);
 			return nValue.getNodeValue();
 		}
 
 		private String getRealCategory(String dirty) {
 			if (dirty != null) {
 				String[] cates = dirty.split("[|]");
				return cates[0];
 			} else {
 				return "";
 			}
 		}
 
 		private static String parseRecencyDate(String datetime) {
 			String[] datetimeSplited = datetime.split("[T]");
 			return datetimeSplited[0];
 		}
 	}
 
 	public static class Reduce extends MapReduceBase implements
 			Reducer<Text, Text, Text, Text> {
 
 		class ValueComparator implements Comparator<String> {
 
 			java.util.Map<String, Integer> base;
 
 			public ValueComparator(java.util.Map<String, Integer> base) {
 				this.base = base;
 			}
 
 			// Note: this comparator imposes orderings that are inconsistent
 			// with equals.
 			public int compare(String a, String b) {
 				if (base.get(a) >= base.get(b)) {
 					return -1;
 				} else {
 					return 1;
 				}
 			}
 		}
 
 		public void reduce(Text key, Iterator<Text> values,
 				OutputCollector<Text, Text> output, Reporter reporter)
 				throws IOException {
 
 			java.util.Map<String, Integer> map = new HashMap<String, Integer>();
 			ValueComparator bvc = new ValueComparator(map);
 			java.util.Map<String, Integer> sorted_map = new TreeMap<String, Integer>(
 					bvc);
 
 			while (values.hasNext()) {
 				String term = values.next().toString();
 				if (map.containsKey(term)) {
 					Integer i = map.get(term);
 					map.put(term, i + 1);
 				} else {
 					map.put(term, 1);
 				}
 			}
 
 			sorted_map.putAll(map);
 
 			//
 			StringBuffer tempSB = new StringBuffer();
 			boolean isFirst = true;
 			// while (values.hasNext()) {
 			// if (!isFirst) {
 			// tempSB.append("$$$");
 			// }
 			// tempSB.append(values.next().toString());
 			//
 			// isFirst = false;
 			// }
 
 			//
 			for (java.util.Map.Entry<String, Integer> entry : sorted_map
 					.entrySet()) {
 				if (!isFirst) {
 					tempSB.append("$$$");
 				}
 				tempSB.append(entry.getKey());
 				tempSB.append("|||").append(entry.getValue());
 
 				isFirst = false;
 			}
 			output.collect(key, new Text(tempSB.toString()));
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		JobConf conf = new JobConf(TagCloud.class);
 		conf.setJobName("TagCloud");
 
 		conf.setOutputKeyClass(Text.class);
 		conf.setOutputValueClass(Text.class);
 
 		conf.setMapOutputKeyClass(Text.class);
 		conf.setMapOutputValueClass(Text.class);
 
 		conf.setMapperClass(Map.class);
 		conf.setCombinerClass(Reduce.class);
 		conf.setReducerClass(Reduce.class);
 
 		conf.setInputFormat(XmlInputFormat.class);
 		conf.setOutputFormat(TextOutputFormat.class);
 		conf.set("xmlinput.start", "<page>");
 		conf.set("xmlinput.end", "</page>");
 
 		FileInputFormat.setInputPaths(conf, new Path(args[0]));
 		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 
 		JobClient.runJob(conf);
 	}
 }
