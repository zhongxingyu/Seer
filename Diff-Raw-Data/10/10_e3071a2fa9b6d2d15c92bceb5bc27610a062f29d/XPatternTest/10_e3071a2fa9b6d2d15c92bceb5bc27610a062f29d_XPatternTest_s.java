 /*using System;
 using System.Collections.Generic;
 using System.Linq;
 using System.Text;
 using System.Text.RegularExpressions;
 using System.Xml;
 using System.IO;*/
 
 import java.io.*;
 
 import javax.xml.*;
 import javax.xml.validation.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.parsers.*;
 
 import java.util.*;
 import java.util.regex.*;
 import org.xml.sax.*;
 import org.w3c.dom.*;
 
 import java.lang.reflect.*;
 
 //Java notes:
 //C# XmlDocument = Java Document
 //C# XmlElement = Java Element
 //C# XmlAttribute = Java Attr
 //C# XmlNode = Java Node
 //C# 
 
 /*Note:  a comment of REFACTOR NOTES suggests a possible refactoring for improved performance */
 
 //namespace MigrationUtils
 //{
     /*class Program
     {
         static void Main(string[] args)
         {
             String testFileName = "C:\\XmlProjects\\dxmath\\dxmath\\boundingbox_ctor_2.xml";
             XmlDocument xd = XmlUtils.loadXML(testFileName);
 
             Console.WriteLine(xd.DocumentElement.Name);
             
             //ne.InnerXml = "bbb";
 
             //Xfinder tests
             XPattern xf = new XPattern(xd);
 
             NodeGroup[] found = xf.findAll("param\\(name)>(datatype)>(*):g");
 
             foreach (NodeGroup eg in found)
             {
                 Console.WriteLine("\nIn Match:");
                 XmlNode[] elms = eg.get();
                 foreach (XmlNode el in elms)
                 {
                     Console.WriteLine(el.Name);
                 }
                 Console.WriteLine("\nIn Group 1:");
                 XmlNode[] elms1 = eg.get(1);
                 foreach (XmlNode el in elms1)
                 {
                     Console.WriteLine(el.Name + ":");
                     XmlElement e2 = xd.CreateElement("name");
                     
                     foreach (XmlAttribute attrib in el.Attributes)
                     {
                         Console.WriteLine(attrib.Name + ": " + attrib.Value);
                     }
                     Console.WriteLine("Text:" + el.InnerXml);
                 }
                 Console.WriteLine("\nIn Group 2:");
                 XmlNode[] elms2 = eg.get(2);
                 foreach (XmlNode el in elms2)
                 {
                     Console.WriteLine(el.Name + ":");
                     foreach (XmlAttribute attrib in el.Attributes)
                     {
                         Console.WriteLine(attrib.Name + ": " + attrib.Value);
                     }
                     Console.WriteLine("Text:" + el.InnerXml);
                 }
                 Console.WriteLine("\nIn Group 3:");
                 XmlNode[] elms3 = eg.get(3);
                 foreach (XmlNode el in elms3)
                 {
                     Console.WriteLine(el.Name + ":");
                     foreach (XmlAttribute attrib in el.Attributes)
                     {
                         Console.WriteLine(attrib.Name + ": " + attrib.Value);
                     }
                     Console.WriteLine("Text:" + el.InnerXml);
                 }
             }
 
             //nodeToText test
             XmlElement ne = xd.CreateElement("Name");
             XmlNode[] n = new XmlNode[3];
             n[0] = xd.CreateTextNode("you ");
             n[1] = xd.CreateElement("amp");
             XmlAttribute ampAt = xd.CreateAttribute("replaceMe");//, "ampersand"
             ampAt.Value = "ampersand";
             n[1].Attributes.Append(ampAt);
             n[2] = xd.CreateTextNode(" me");
 
             foreach (XmlNode xn in n)
                 ne.AppendChild(xn);
 
             XPattern x2 = new XPattern(ne);
 
             String[] pm = {"me"};
             String[] pm2 = { "and" };
             NodeGroup ng = x2.findFirst("\"/0", pm);
 
             NodeGroup ng2 = x2.findFirst("@replaceMe$/0", pm2);
 
             Console.WriteLine(ng.get()[0].OuterXml);
             Console.WriteLine(ng2.get()[0].OuterXml);
 
             Console.WriteLine("<Name>" + ne.InnerXml + "</Name>");
 
             x2.replace("amp", "ampersand");
 
             Console.WriteLine("<Name>" + ne.InnerXml + "</Name>");
 
             //XmlUtils.nodeToText(xd, ne, "ampersand", "&");
 
             x2.replace("(ampersand)", "metadata\\title\\\"My title/<category>abstract/>$1");
 
             Console.WriteLine("<Name>" + ne.InnerXml + "</Name>");
 
 
 
             Console.ReadKey();
         }
 
         
 
         
 
         
         
 
         
     }*/
 
 	//we need a LinkedList that doesn't throw an exception if an item isn't found
 	class ELinkedList<T> extends LinkedList<T>
 	{
 		public ELinkedList(ELinkedList<T> in)
 		{
 			super(in);
 		}
 		
 		public ELinkedList()
 		{
 			super();
 		}
 		
 		public T get(int in)
 		{
 			if(in >= size())
 				return null;
 			else
 				return super.get(in);
 		}
 	}
 
 	class MatchCollection
 	{
 		Matcher m;
 		
 		public MatchCollection()
 		{
 			m = null;
 		}
 		
 		public void setMatcher(Matcher in)
 		{
 			m = in;
 		}
 		
 		public Matcher getMatcher()
 		{
 			return m;
 		}
 	}
 
     class XtocRunner
     {
         String dirName;
         String xtocFilename;
 
         public XtocRunner(String xtocFile)
         {
             xtocFilename = xtocFile;
             dirName = DirectoryRunner.getDirName(xtocFile);
         }
 
         public void processXtoc()
         {
              String[] all = XmlUtils.getXtocFiles(xtocFilename);
              for (String entry : all)
              {
                  //entry = entry.Substring(7);
                  //String fname = entry.Substring(7);
                  //fname = "C:\\XmlProjects\\dxmath\\dxmath\\" + fname;
                  String fname = dirName + "\\" + entry;
                  processFile(fname);
              }
         }
 
         public void processFile(String filePath)
         {
         }
 
     }
 
     class DirectoryRunner
     {
         public DirectoryRunner()
         {
         }
 
         public void processDir(String dirName)
         {
             //walkDir(dirName, null);
         }
 
         public void processDir(String dirName, String pattern)
         {
             //walkDir(dirName, pattern);
         }
 
         /*private void walkDir(String dirName, String pattern)
         {
             // Subdirs
             dirPreProcess(dirName);
              
             //DirectoryInfo dInfo = new DirectoryInfo(dirName);
             try         // Avoid errors such as "Access Denied"            
             {                
                 for (DirectoryInfo inDir : dInfo.GetDirectories())                
                 {                    
                     //if (inDir.Name.StartsWith(Pattern))                        
                         //Console.WriteLine("Found dir:  " + inDir.FullName);                     
                     walkDir(inDir.FullName, pattern);                
                 }            
             }            
             catch (IOException ex)            
             {            }             
             // Subfiles            
             try         // Avoid errors such as "Access Denied"            
             {                
                 for (FileInfo iInfo : dInfo.GetFiles())                
                 {
                     if (pattern == null || iInfo.Name.StartsWith(pattern))
                         processFile(dirName + "\\" + iInfo.Name);                
                 }            
             }            
             catch (IOException ex)            
             {            }
 
             dirPostProcess(dirName);
         }*/
 
         public void dirPreProcess(String dirName)
         {
         }
 
         public void dirPostProcess(String dirName)
         {
         }
 
         public void processFile(String filePath)
         {
         }
 
         public static String getFileName(String path)
         {
             String[] parts = path.split("\\");
             return parts[parts.length - 1];
         }
 
         public static String getDirName(String path)
         {
             String[] parts = path.split("\\");
             String rtnVal = parts[0];
             for (int i = 1; i < parts.length - 1; i++)
                 rtnVal += "\\" + parts[i];
             return rtnVal;
         }
     }
 
     class XmlUtils
     {
         public static void saveXML(Document data, String toFile)
         {
         	try
         	{
         		TransformerFactory factory = TransformerFactory.newInstance(); 
         		Transformer transformer = factory.newTransformer(); 
 
         		//transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
         		//transformer.setOutputProperty(OutputKeys.METHOD,"xml"); 
         		// transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3"); 
 
 
         		// create string from xml tree 
         		StringWriter sw = new StringWriter(); 
         		StreamResult result = new StreamResult(sw); 
         		DOMSource source = new DOMSource(data); 
         		transformer.transform(source, result); 
 
         		String xmlString = sw.toString(); 
 
 
         		File file = new File(toFile); 
         		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))); 
         		bw.write(xmlString); 
         		bw.flush();
         		bw.close();
         	}
         	catch(Exception ex)
         	{
         		ex.printStackTrace();
         	}
         }
 
         public static Document loadXML(String fromFile)
         {
             /*Document xd = new Document();
             StreamReader sr = File.OpenText(fromFile);
             String stuff = sr.ReadToEnd();
             xd.LoadXml(stuff);
             sr.Close();
             return xd;*/
         	try
         	{
         		DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
         		parserFactory.setNamespaceAware(true);
         		DocumentBuilder parser = parserFactory.newDocumentBuilder();
         		Document document = parser.parse(new File(fromFile));
         		return document;
         	}
         	catch(Exception ex)
         	{
         		ex.printStackTrace();
         		return null;
         	}
         	
         }
 
         public static void saveText(String txt, String toFile)
         {
             // create a writer and open the file
         	/*BufferedWriter bw = new BufferedWriter();
             TextWriter tw = new StreamWriter(toFile);
             txt = txt.Replace("\n", System.Environment.NewLine);
             tw.Write(txt);
             // close the stream
             tw.Close();*/
         	try
         	{
         		BufferedWriter bw = new BufferedWriter(new FileWriter(toFile));
         		bw.write(txt);
         		bw.close();
         	}
         	catch(Exception ex)
         	{
         		ex.printStackTrace();
         	}
         }
 
         public static String readText(String fromFile) throws IOException
         {
         	/*
             TextReader tr = new StreamReader(fromFile);
 
             String rtnVal = "";
             // read a line of text
             while (true)
             {
                 String line = tr.ReadLine();
                 if (line == null)
                     break;
                 rtnVal += line + "\n";
             }
             // close the stream
             tr.Close();*/
         	String rtnVal = "";
         	BufferedReader br = new BufferedReader(new FileReader(fromFile));
         	while(true)
         	{
         		String line = br.readLine();
         		if(line == null)
         			break;
         		rtnVal += line + '\n';
         	}
         	br.close();
             return rtnVal;
         }
 
         public static String getText(int nodeNo, Element el)
         {
             //int i = 0;
             NodeList nl = el.getChildNodes();
             for (int i = 0; i < nl.getLength(); i++)
             {
             	Node xn = nl.item(i);
                 if (xn.getNodeType()== Node.TEXT_NODE)
                 {
                     if (i == nodeNo)
                         return xn.getTextContent();
                 }
                 //i++;
             }
             return "";
         }
 
         public static String[] grabAttribs(String[] select, Element el)
         {
             String[] rtnVal = new String[select.length];
             int i = 0;
             for (String s : select)
             {
                 rtnVal[i] = el.getAttribute(s);
                 i++;
             }
             return rtnVal;
         }
 
         public static void packTextNodes(Node input)
         {
             XPattern xp1 = new XPattern(input);
             while (true)
             {
                 NodeGroup ng = xp1.findFirst("\"*>\"*");
                 if (ng == null)
                     break;
                 Node[] oldTexts = new Node[2];
                 for (int i = 0; i < 2; i++)
                     oldTexts[i] = ng.get()[i];
                 String newText = oldTexts[0].getTextContent() + oldTexts[1].getTextContent();
                 oldTexts[0].setTextContent(newText);// = newText;
                 oldTexts[1].getParentNode().removeChild(oldTexts[1]);
             }
         }
 
         public static String[] textNodeDiff(Document x1, Document x2, boolean firstAsXml, boolean secondAsXml)
         {
             return textNodeDiff(x1, x2, null, firstAsXml, secondAsXml);
         }
 
         public static String[] textNodeDiff(Document x1, Document x2, String[] excludeParents, boolean firstAsXml, boolean secondAsXml)
         {
             String[] xps = new String[0];
             if(excludeParents != null)
                 xps = excludeParents;
             XPattern xp1; 
             NodeGroup[] ng1 = null; 
             Vector<String> list1 = new Vector<String>();
 
             if(x1 != null)
             {
                 packTextNodes(x1.getDocumentElement());
                 xp1 = new XPattern(x1);
                 ng1 = xp1.findAll("\"*");
                 for (NodeGroup n1 : ng1)
                 {
                     Node xn = n1.get()[0];
                     String nodeText = "";
                     if (firstAsXml)
                         nodeText = XmlUtils.outerXml(xn);//nodeText = xn.OuterXml;
                     else
                         nodeText = xn.getTextContent();
                     /*if (nodeText.Trim().Equals("<="))
                         Console.WriteLine("");
                     if(nodeText.Trim().Equals("&lt;="))
                        Console.WriteLine("");*/
                     String par = xn.getParentNode().getNodeName();
                     boolean putMe = true;
                     for (String s : xps)
                     {
                         if (s.equals(par))
                             putMe = false;
                     }
                     if (putMe)
                         list1.add(nodeText.trim());
                 }
             }
 
 
             XPattern xp2;
             NodeGroup[] ng2 = null;
             Vector<String> list2 = new Vector<String>();
 
             if(x2 != null)
             {
                 packTextNodes(x2.getDocumentElement());
                 xp2 = new XPattern(x2);
                 ng2 = xp2.findAll("\"*");
                 for (NodeGroup n2 : ng2)
                 {
                     Node xn = n2.get()[0];
                     String par = xn.getParentNode().getNodeName();
                     String nodeText = "";
                     if (secondAsXml)
                         nodeText = XmlUtils.outerXml(xn);//nodeText = xn.OuterXml;
                     else
                         nodeText = xn.getTextContent();
                     /*if (nodeText.Trim().Equals("<="))
                         Console.WriteLine("");
                     if (nodeText.Trim().Equals("&lt;="))
                         Console.WriteLine("");*/
                     boolean putMe = true;
                     for (String s : xps)
                     {
                         if (s.equals(par))
                             putMe = false;
                     }
                     if (putMe)
                         list2.add(nodeText.trim());
                 }
             }
 
             Vector<String> listMoved = new Vector<String>();
             
             
 
             boolean found = false;
             for (int i = 0; i < list1.size(); i++)
             {
                 //var node1 = list1[i];
                 String val1 = list1.get(i);
                 found = false;
                 for (int j = 0; j < list2.size(); j++)
                 {
                     //var node2 = list2[j];
                     String val2 = list2.get(j);
                     if (val1.equals(val2))
                     {
                         if (j < i)
                             listMoved.add(list2.get(j));
                         list1.remove(i);
                         i--;
                         list2.remove(j);
                         j--;
                         break;
                     }
                 }
                 if (found)
                     break;
             }
             String[] rtnVal = new String[3];
             rtnVal[0] = "";
             //rtnVal[0] = "Items in document 1 that are not in document 2:\n";
             for (String n : list1)
                 rtnVal[0] += "=> " + n + "\n";
             rtnVal[1] = "";
             //rtnVal[1] = "Items in document 2 that are not in document 1:\n";
             for (String n : list2)
                 rtnVal[1] += "=> " + n + "\n";
             rtnVal[2] = "";
             //rtnVal[2] = "Items that were moved:\n";
             for (String n : listMoved)
                 rtnVal[2] += "=> " + n + "\n";
 
             return rtnVal;
         }
 
         public static void nodeToText(Node src, String oldElement, String newText)
         {
             XPattern xf = new XPattern(src);
             Document xd = src.getOwnerDocument();
             NodeGroup[] eggs = xf.findAll(oldElement + "/(*):g");
             for (NodeGroup grp : eggs)
             {
                 Node parent = grp.get(1)[0];
                 Node oldChild = grp.get()[0];
                 Node newChild = xd.createTextNode(newText);
                 parent.replaceChild(newChild, oldChild);
                 parent.toString();
             }
         }
 
         public static String[] getXtocFiles(String xtocFile)
         {
             Document xtoc = XmlUtils.loadXML(xtocFile);
             XPattern xp1 = new XPattern(xtoc);
             NodeGroup[] ns = xp1.findAll("@topicURL");
             String[] rtnVal = new String[ns.length];
             for (int i = 0; i < ns.length; i++)
             {
                 Element el = (Element) ns[i].get()[0];
                 String val = el.getAttribute("topicURL");
                 rtnVal[i] = val;
             }
             return rtnVal;
         }
         
         public static void insertAfter(Node parent, Node newNode, Node refNode)
         {
         	if(parent.getLastChild() == refNode)
         		parent.appendChild(newNode);
         	else
         		parent.insertBefore(newNode, refNode.getNextSibling());
         }
         
         public static String outerXml(Node refNode)
         {
         	//String rtnVal = "";
         	switch(refNode.getNodeType())
         	{
         	case Node.ATTRIBUTE_NODE:
         		return " " + refNode.getNodeName() + "=\"" + refNode.getNodeValue() + "\"";
         	case Node.CDATA_SECTION_NODE:
         		return "<![CDATA[" + refNode.getNodeValue() + "]]>";
         	case Node.COMMENT_NODE:
         		return refNode.getNodeValue();
         	case Node.TEXT_NODE:
         		return refNode.getNodeValue();
         	case Node.PROCESSING_INSTRUCTION_NODE:
         		return refNode.getNodeValue();
         	case Node.ELEMENT_NODE:
         		String rtnVal = "<" + refNode.getNodeName();
         		//boolean endedFront = false;
         		for(int i = 0; i < refNode.getAttributes().getLength(); i++)
         			rtnVal += " " + refNode.getAttributes().item(i);
         		if(refNode.hasChildNodes())
         		{
         			rtnVal += ">";
         			for(int i = 0; i < refNode.getChildNodes().getLength(); i++)
         				rtnVal += XmlUtils.outerXml(refNode.getChildNodes().item(i));
         			rtnVal += "</" + refNode.getNodeName() + ">"; 
         		}
         		else
         			rtnVal += " />";
         		return rtnVal;
         	default:
         		System.out.println("Unhandled Nodetype: " + refNode.getNodeType());
         		return "";
         	}
         	
         }
 
     }
 
     class RegexUtil
     {
 
     }
 
     class CommandUtils
     {
         public static String run(Object command)  //Despite being well-commented, this function doesn't work
         {
         	return null;
             /*String rtnVal = "";
             try
             {
                 // create the ProcessStartInfo using "cmd" as the program to be run,
                 // and "/c " as the parameters.
                 // Incidentally, /c tells cmd that we want it to execute the command that follows,
                 // and then exit.
                 System.Diagnostics.ProcessStartInfo procStartInfo =
                     new System.Diagnostics.ProcessStartInfo("cmd", "/c " + command);
 
                 // The following commands are needed to redirect the standard output.
                 // This means that it will be redirected to the Process.StandardOutput StreamReader.
                 procStartInfo.RedirectStandardOutput = true;
                 procStartInfo.UseShellExecute = false;
                 // Do not create the black window.
                 procStartInfo.CreateNoWindow = true;
                 // Now we create a process, assign its ProcessStartInfo and start it
                 System.Diagnostics.Process proc = new System.Diagnostics.Process();
                 proc.StartInfo = procStartInfo;
                 proc.Start();
                 // Get the output into a string
                 string result = proc.StandardOutput.ReadToEnd();
                 String errors = proc.StandardError.ReadToEnd();
                 rtnVal = result + "\n" + errors;
                 // Display the command output.
                 Console.WriteLine(result);
             }
             catch (Exception objException)
             {
                 // Log the exception
                 Console.WriteLine(objException.Message);
             }
             return rtnVal;*/
         }
     }
 
     class StringUtils
     {
         public static String lCase(String input)
         {
             String rtnVal = "";
             for (int i = 0; i < input.length(); i++)
                 rtnVal += Character.toLowerCase(input.charAt(i));
             return rtnVal;
         }
 
         public static String lcFirst(String input)
         {
             String rtnVal = "";
             rtnVal += Character.toLowerCase(input.charAt(0));
             for (int i = 1; i < input.length(); i++)
                 rtnVal += input.charAt(i);
             return rtnVal;
         }
 
         public static String uCase(String input)
         {
             String rtnVal = "";
             for (int i = 0; i < input.length(); i++)
                 rtnVal += Character.toUpperCase(input.charAt(i));
             return rtnVal;
         }
 
         public static String ucFirst(String input)
         {
             String rtnVal = "";
             rtnVal += Character.toUpperCase(input.charAt(0));
             for (int i = 1; i < input.length(); i++)
                 rtnVal += input.charAt(i);
             return rtnVal;
         }
     }
 
     class XPattern
     {
         Node xd;
         //ElementGroup[] eggs;
 
         int globalMatch = 1;
         boolean preserveRoot = false;
         
         int options = 0;
         
         public static final int IGNORE_COMMENTS = 1;
         public static final int IGNORE_EMPTY_TEXT = 2;
         public static final int GLOBAL_SEARCH = 4;
         public static final int IGNORE_CASE = 8;
 
         public XPattern(Document d)
         {
             xd = d.getDocumentElement();
         }
 
         public XPattern(Node nde)
         {
             xd = nde;
         }
         
         public void setOption(int input)
         {
        	options = options ^ input;
         }
         
         private String setupOptions(String input, boolean ignoreGlobal)
         {
         	String options = "";
        	this.options = 0;
             String pattern = input;
             if (input.contains("|"))
             {
                 String[] allIn = input.split("\\|");
                 pattern = allIn[0];
                 options = allIn[1];
             }
             
             for(char c : options.toCharArray())
             {
             	switch(c)
             	{
             	case 'g': if(!ignoreGlobal) setOption(XPattern.GLOBAL_SEARCH);  break;
             	case 'i': setOption(XPattern.IGNORE_CASE); break;
             	case 'c': setOption(XPattern.IGNORE_COMMENTS); break;
             	case 't': setOption(XPattern.IGNORE_EMPTY_TEXT); break;
             	}
             }
             
             return pattern;
         }
 
         /*public void setPreserveRootChildren(bool val) //obsolete
         {
             preserveRoot = val;
         }*/
 
         public NodeGroup findFirst(String pattern)
         {
             return findFirst(pattern, null);
         }
 
         public NodeGroup findFirst(String pattern, String[] ps)
         {
             globalMatch = 1;
             ELinkedList<XTreeNode> lst = tokenizeFind(pattern, true);
             return XTree(xd, lst, ps);
         }
 
         public NodeGroup[] findAll(String input)
         {
             return findAll(input, null);
         }
 
         public NodeGroup[] findAll(String input, String[] ps)
         {
             globalMatch = 1;
             ELinkedList<NodeGroup> elist = new ELinkedList<NodeGroup>();
             String options = "";
             String pattern = input;
             if (input.contains("|"))
             {
                 String[] allIn = input.split("|");
                 pattern = allIn[0];
                 options = allIn[1];
                 if (!options.contains("g"))
                     options += "g";
             }
             else
                 input += "|g";
 
             ELinkedList<XTreeNode> lst = tokenizeFind(input, false);
             
             while (true)
             {
                 NodeGroup eg = XTree(xd, lst, ps);
                 if (eg == null)
                     break;
                 else
                     elist.addLast(eg);
             }
             NodeGroup ngs[] = new NodeGroup[0];
             return elist.toArray(ngs);
         }
 
         /*REFACTOR NOTES: make sure that the pattern isn't tokenized each time this method is called */
         private ELinkedList<XTreeNode> tokenizeFind(String input, boolean ignoreGlobal)
         {
         	String pattern = setupOptions(input, ignoreGlobal);
         	ELinkedList<XTreeNode> lst = new ELinkedList<XTreeNode>();
             String currToken = "";
             int max = 1;
             ELinkedList<Integer> groupNo = new ELinkedList<Integer>();
 
             for (int i = 0; i < pattern.length(); i++)
             {
                 char c = pattern.charAt(i);
 
                 switch (c)
                 {
                     case '/':
                         char d = '0';
                         if(i > 0)
                             d = pattern.charAt(i - 1);
                         if (d == '!' || d == '=' || d == '\"')
                         {
                             currToken += c;
                             break;
                         }
                         else
                         {
                             if (currToken != "")
                             {
                                 lst.addLast(new XTreeNode(currToken, groupNo));
                                 currToken = "";
                             }
                             lst.addLast(new XTreeNode(String.valueOf(c), groupNo));
                             break;
                         }
                     case '\\':
                     case '<':
                     case '>':
                     //case '#':
                     case '@':
                     case '=':
                     case '\"':
                         if (currToken != "")
                             lst.addLast(new XTreeNode(currToken, groupNo));
                         lst.addLast(new XTreeNode(String.valueOf(c), groupNo));
                         currToken = "";
                         break;
                     case '#':
                     	if (currToken != "")
                             lst.addLast(new XTreeNode(currToken, groupNo));
                         //lst.addLast(new XTreeNode(String.valueOf(c), groupNo));
                     	while(true)
                     	{
                     		i++;
                     		if(i == pattern.length())
                     			break;
                     		char ch = pattern.charAt(i);
                     		if(Character.isDigit(ch))
                     			currToken += ch;
                     		else
                     		{
                     			lst.addLast(new XTreeNode(currToken, groupNo));
                     			i--;
                     			break;
                     		}
                     	}
                         currToken = "";
                         break;
                     case '(':
                         groupNo.add(max);
                         max++;
                         break;
                     case ')':
                         if (currToken != "")
                             lst.addLast(new XTreeNode(currToken, groupNo));
                         currToken = "";
                         groupNo.removeLast();
                         break;
                     default:
                         currToken += c;
                         if (currToken.equals("CDATA"))  //CDATA is a special token
                         {
                             lst.addLast(new XTreeNode(currToken, groupNo));
                             currToken = "";
                         }
                         break;
                 }
             }
             if (currToken != "")
                 lst.addLast(new XTreeNode(currToken, groupNo));
             return lst;
         }
         
         private NodeGroup XTree(Node doc, ELinkedList<XTreeNode> inList, String[] paramStrings)
         {
             
             
             XWalker xw = new XWalker(doc, inList, paramStrings);
             /*String offOptions = "gi";
             for (char c : options.toCharArray())
             {
                 switch (c)
                 {
 
                     case 'g':
                         xw.setLastMatch(globalMatch);
                         offOptions = offOptions.replace('g', ' ');
                         break;
                 }
             }
             for (char c : offOptions.toCharArray())
             {
                 switch (c)
                 {
                     case 'g':
                         globalMatch = 1;
                         break;
                 }
             }*/
             if((this.options & GLOBAL_SEARCH) > 0)
             	xw.setLastMatch(globalMatch);
             else
             	globalMatch = 1;
             if((this.options & IGNORE_CASE) > 0)
             	xw.setOption(IGNORE_CASE);
             if((this.options & IGNORE_COMMENTS) > 0)
             	xw.setOption(IGNORE_COMMENTS);
             if((this.options & IGNORE_EMPTY_TEXT) > 0)
             	xw.setOption(IGNORE_EMPTY_TEXT);
             
             xw.start();
             if (xw.getEGroup() != null)
                 globalMatch++;
             return xw.getEGroup();
 
         }
 
 //  substitution
         public void replaceNode(Node input, String replacePattern, String[] paramList)
         {
             XReplace(input, replacePattern, paramList);
         }
 
         public void replaceNode(Node input, String replacePattern)
         {
             replaceNode(input, replacePattern, null);
         }
 
         public NodeGroup replace(String findPattern, String replacePattern, String[] paramList)
         {
             NodeGroup eg = findFirst(findPattern);
             if(eg != null)
                 XReplace(eg, tokenizeReplace(replacePattern), paramList);
             return eg;
         }
 
         public NodeGroup replace(String findPattern, String replacePattern)
         {
             return replace(findPattern, replacePattern, null);
         }
 
         /*private NodeGroup[] sortNodes(NodeGroup[] list) //incorrect approach is obsolete
         {
             for (int i = 0; i < list.Length - 1; i++)
             {
                 int a = list[i].getDepth();
                 for (int j = i + 1; j < list.Length; j++)
                 {
                     int b = list[j].getDepth();
                     if (a < b)
                     {
                         NodeGroup temp = list[i];
                         list[i] = list[j];
                         list[j] = temp;
                     }
                 }
             }
             return list;
         }*/
 
         public NodeGroup[] replaceAll(String findPattern, String replacePattern, String[] paramList)
         {
             globalMatch = 1;
             ELinkedList<NodeGroup> ngs = new ELinkedList<NodeGroup>();
             //do NOT assume global search
 
             /*String options = "";
             String pattern = findPattern;
             if (findPattern.Contains("|"))
             {
                 String[] allIn = findPattern.Split('|');
                 pattern = allIn[0];
                 options = allIn[1];
                 if (!options.Contains("g"))
                     options += "g";
             }
             else
                 findPattern += "|g";*/  
 
             //NodeGroup[] ngs = findAll(findPattern);
             //do replace on the deepest nodes first so that replacements aren't being made on subnodes of already removed nodes
             //ngs = sortNodes(ngs);
             
             ELinkedList<XTreeNode> findList = tokenizeFind(findPattern, false);
             Vector<XTreeNode> replaceList = tokenizeReplace(replacePattern);
 
             while (true)
             {
                 NodeGroup eg = XTree(xd, findList, paramList);
                 if (eg == null)
                     break;
                 XReplace(eg, replaceList, paramList);
                 //System.out.println(XmlUtils.outerXml(xd));
                 ngs.addLast(eg);
             }
             int n = ngs.size();
             NodeGroup[] arr = new NodeGroup[0];
             return ngs.toArray(arr);
         }
 
         public NodeGroup[] replaceAll(String findPattern, String replacePattern)
         {
             return replaceAll(findPattern, replacePattern, null);
         }
 
         private Node XReplace(Node branch, String pattern, String[] paramList)
         {
             NodeGroup ng = new NodeGroup(branch);
             
             return XReplace(ng, tokenizeReplace(pattern), paramList);
         }
 
         class XmlNodeEntry
         {
             private Node nde;
             private char dir;
 
             public XmlNodeEntry(Node n, char dd)
             {
                 nde = n;
                 dir = dd;
             }
 
             public char getDir()
             {
                 return dir;
             }
 
             public Node getNode()
             {
                 return nde;
             }
 
         }
         
         //private Node getNodeInChain()
         
         private Object[] getValueInChain(String token, String[] paramList, NodeGroup ng) //returns a String and a Node
         {
         	
         	
         	String rtnVal = null;
         	Node selNode = null;
         	int groupToken = 0;
             boolean isParamString = false;
             if(token.startsWith("#"))
             	isParamString = true;
             
             token = token.substring(1);
             if (token.startsWith("`"))
             {
             	token = token.substring(1);
                 groupToken = 1;
             }
             if (token.startsWith("\'"))
             {
             	token = token.substring(1);
                 groupToken = 2;
             }
 
             int groupNo = Integer.parseInt(token);
             
             if(isParamString)
             {
             	rtnVal = paramList[groupNo];
             	
             	//valueOnly = true;
             }
             else
             {
             	selNode = ng.get(groupNo)[groupToken];
             	switch(selNode.getNodeType())
             	{
             	case Node.TEXT_NODE:
             	case Node.CDATA_SECTION_NODE:
             		rtnVal = selNode.getNodeValue();
             		//break;
             	case Node.ATTRIBUTE_NODE:
             		Attr myAttrib = (Attr) selNode;
                     if (myAttrib.getName().equals("__String__Value__Only__Attribute__"))
                         rtnVal = myAttrib.getValue();
                     //else
                     	//return null;
                     //break;
             	//default:
             		//return null;
             		//break;
             	}
             }
             Object[] rtnObj = new Object[2];
             rtnObj[0] = rtnVal;
             rtnObj[1] = selNode;
             return rtnObj;
         }
         
         private Vector<XTreeNode> tokenizeReplace(String pattern)
         {
         	Vector<XTreeNode> lst = new Vector<XTreeNode>();
         	String currToken = "";
         	for (int i = 0; i < pattern.length(); i++)
             {
                 char c = pattern.charAt(i);
 
                 switch (c)
                 {
                     case '/':
                     case '\\':
                     case '<':
                     case '>':
                         if (currToken != "")
                         {
                             lst.add(new XTreeNode(currToken, null));
                             currToken = "";
                         }
                         lst.add(new XTreeNode(String.valueOf(c), null));
                         break;
                     case '@':
                         //case '*':
                         if (currToken != "")
                         {
                             lst.add(new XTreeNode(currToken, null));
                             currToken = "";
                         }
                         //lst.AddLast(new XTreeNode(Convert.ToString(c), null));
                         currToken = "@";
                         break;
                     case '$':
                     case '%':
                     case '&':
                     case '^':
                     case '#':
                         int tokenNo = 0;
                         int tokenVal = 0;
                         String prefix = String.valueOf(c);
                         //lst.Last.Value.getToken().StartsWith("@")
                         if (currToken != "")
                         {
                             lst.add(new XTreeNode(currToken, null));
                             currToken = "";
                         }
                         /*if (i < pattern.Length && pattern[i + 1] == '\"')
                         {
                             prefix = '$';
                             i++;
                         }*/
                         if (pattern.charAt(i + 1) == '`' || pattern.charAt(i + 1) == '\'')
                         {
                         	prefix += pattern.charAt(i + 1);
                             i++;
                         }
                         int tempIdx = i + 1;
                         do
                         {
                             tokenVal *= 10;
                             tokenVal += tokenNo;
                             i++;
                             if (i == pattern.length())
                                 break;
                             c = pattern.charAt(i);
                             try
                             {
                             	tokenNo = Integer.parseInt(String.valueOf(c));
                             }
                             catch(NumberFormatException ex)
                             {
                             	break;
                             }
                         } while (true);
                         if (i == tempIdx)
                         {
                             //if (c == '$' || c == '"') //$$ = innerXML, $" = innerText
                             //lst.AddLast(new XTreeNode("$" + c, null));
                             //else
                             //i--;
                         }
                         else
                             lst.add(new XTreeNode(prefix + String.valueOf(tokenVal), null));
                         i--;
                         break;
                     default:
                         currToken += c;
                         break;
                 }
             }
 
             if (currToken != "")
                 lst.add(new XTreeNode(currToken, null));
             
             return lst;
         }
 
         /*REFACTOR NOTES: Make sure the replacement pattern isn't tokenized each time */
         private Node XReplace(NodeGroup g, Vector<XTreeNode> lst, String[] paramList)
         {
             Node branch = g.get()[0];
             Document xd = branch.getOwnerDocument();
             //Vector<XTreeNode> lst = new Vector<XTreeNode>();
             //String currToken = "";
 
             //store off the indeces of all captured nodes, as well as ancestry
             Hashtable<Node, Vector<Node>> clonedParents = new Hashtable<Node, Vector<Node>>();
             Hashtable<Node, Vector<Integer>> insertionPoints = new Hashtable<Node, Vector<Integer>>();
             Hashtable<Node, Vector<Integer>> oldAncestry = new Hashtable<Node, Vector<Integer>>();
             Hashtable<Node, Vector<XmlNodeEntry>> insertionNodes = new Hashtable<Node, Vector<XmlNodeEntry>>();
             //int yy = g.getGroupCount();
             //if (yy > 1)
                 //Console.WriteLine("");
             for (int i = 0; i < g.getGroupCount(); i++)
             {
                 Node child = g.get(i + 1)[0];  //this could be a problem; review the code to see why this is necessary
                 if (child.getNodeType() == Node.ATTRIBUTE_NODE)
                     continue;
                 Node parent = child.getParentNode();
                 int idx = 0;
                 NodeList nl1 = parent.getChildNodes();
                 for (int j = 0; j < nl1.getLength(); j++)
                 {
                 	Node ch = nl1.item(j);
                     if (ch == child)
                         break;
                     idx++;
                 }
                 if (!clonedParents.containsKey(parent))
                 {
                     clonedParents.put(parent, new Vector<Node>());
                     insertionPoints.put(parent, new Vector<Integer>());
                     oldAncestry.put(parent, new Vector<Integer>());
                 }
                 else
                     System.out.println("");
                 insertionPoints.get(parent).add(idx);
                 oldAncestry.get(parent).add(i + 1);
             }
 
             //int insertionPoint = 0;
             /*NodeList nl2 = branch.getParentNode().getChildNodes();  //this loop does nothing
             for (int j = 0; j < nl2.getLength(); j++)
             {
             	Node ch = nl2.item(j);
                 if (ch == branch)
                     break;
                 //insertionPoint++;
             }*/
 
            
             //XmlNode subCursor = src;
             Node newNode = null;
             Node refNode = null;
             Node topNode = null;
 
             
 
             char replaceDir = '0';
 
             
 
             
             int tokenNo = 0;
             //if the first one is a ^, move the insertion point to the specified node
             if (lst.size() > 0 && lst.get(0).getToken().startsWith("^"))
             {
                 String tok = lst.get(0).getToken();
                 tok = tok.substring(1);
                 int groupNo = Integer.parseInt(tok);
                 branch = g.get(groupNo)[0];
                 //lst.remove(0); do NOT do this
                 tokenNo++;
             }
 
             //now remove all the nodes after the branch
             boolean afterBranch = false;
             for (Node nde : g.get())
             {
                 if (nde.getNodeType() == Node.ATTRIBUTE_NODE)
                     continue;
                 if (afterBranch)
                     nde.getParentNode().removeChild(nde);
                 if (nde == branch)
                     afterBranch = true;
             }
 
             if (lst.size() == 0)  //replace me with nothing - just cut the branch and be done with it
             {
                 branch.getParentNode().removeChild(branch);
                 return null;
             }
 
             //process the replacement tokens
             for (; tokenNo < lst.size(); tokenNo++)
             {
             	XTreeNode token = lst.get(tokenNo);
                 String str = token.getToken();
                 boolean processMe = true;
                 boolean replaceMe = true;
                 
                 if (str.length() == 1)
                 {
                     switch (str.charAt(0))
                     {
                         case '/':
                             refNode = refNode.getParentNode();
                             processMe = false;
                             //insertionPoint = -1;
                             break;
                         case '\\':
                             /*foreach(XmlNode kk in clonedParents.Keys)
                             {
                                 if (clonedParents[kk].Contains(refNode))
                                     insertionPoint = insertionPoints[kk];
                             }*/
                             replaceDir = str.charAt(0);
                             processMe = false;
                             break;
                         case '>':
                         case '<':
                             replaceDir = str.charAt(0);
                             processMe = false;
                             //insertionPoint = -1;
                             break;
                         case '=':   //this could be orphaned in a @$x=$y chain; just skip it
                             processMe = false;
                             break;
                     }
 
                 }
                 if(processMe)
                 {
                     if (str.startsWith("$") || str.startsWith("#"))
                     {
                         int tokenIdx = tokenNo;
                         String context = "";
                         
                         if(tokenIdx > 0)
                             context = lst.get(tokenIdx - 1).getToken();
 
                         String textValue = "";
                         boolean valueOnly = true;
                         Node selNode = null; 
                         int groupStart = 1;
                         if(str.contains("`") || str.contains("'"))
                         	groupStart++;
                         int groupNo = Integer.parseInt(str.substring(groupStart));
                         while(valueOnly)
                         {
                         	Object[] stuff = getValueInChain(str, paramList, g);
                         	String strValue = (String) stuff[0];
                         	selNode = (Node) stuff[1];
                         	if(strValue == null)
                         		valueOnly = false;
                         	else
                         	{
                         		textValue += strValue;
                         		tokenNo++;
                         		if(tokenNo == lst.size())
                         			break;
                                 str = lst.get(tokenNo).getToken();
                                 char c = str.charAt(0);
                                 if(!(c == '#' || c == '$'))
                                 {
                                 	tokenNo--;
                                 	break;
                                 }
                         	}
                         
                         
                         
                         }
                         /*if(valueOnly)
                         {
                         	
                         }*/
                         
                         
                         
                         replaceMe = false;
                         if (valueOnly || selNode.getNodeType() != Node.ELEMENT_NODE)
                         {
                             //Attr myAttrib = null;
                             
                             
                             
                             if (context.endsWith("@"))  //@$n
                             {
                             	Element el = (Element) refNode;
                                 if (valueOnly)  //making an attribute from text
                                 {
                                     Attr newAttrib = xd.createAttribute(textValue);
                                     el.setAttribute(newAttrib.getName(), newAttrib.getValue());
                                 }
                                 else  //copying attribute(s) in
                                 {
                                 	Node[] nodes = g.get(groupNo);
                                 	for(int n = 0; n < nodes.length; n++)
                                 	{
                                 		Attr copiedAttrib = (Attr) nodes[n];
                                 		el.setAttribute(copiedAttrib.getName(), copiedAttrib.getValue());
                                 	}
                                 }
                             }
                             else if (context.contains("="))  //@xxx=$n (now fixed)
                             {
                                 String preText = context.substring(context.indexOf("=") + 1);
                                 String attribName = context.substring(0, context.indexOf("="));
                                 Element el = (Element) refNode;
                                 //you need to keep going backwards until you find the context with @
                                 int preContext = 0;
                                 while(!attribName.startsWith("@"))
                                 {
                                 	preContext++;
                                 	String tempContext = lst.get(tokenIdx - preContext).getToken();  //should be the same as context
                                 	char c = tempContext.charAt(0);
                                 	
                                 	if(c == '=')
                                 		continue;
                                 	if(!(c == '$' || c == '#'))
                                 	{
                                 		attribName = tempContext + attribName;
                                 		continue;
                                 	}
                                 	else
                                 	{
                                 		Object[] myStuff = getValueInChain(tempContext, paramList, g);
                                 		tempContext = (String) myStuff[0];
                                 		Node myNode = (Node) myStuff[1];
                                 		if(tempContext != null)
                                 			attribName = tempContext + attribName;
                                 		else if(myNode.getNodeType() == Node.ATTRIBUTE_NODE)
                                 			attribName = myNode.getNodeName();
                                 	}
                                 	//get the text value from the $ or #
                                 	//tempContext = tempContext.substring(1);
                                 }
                                 attribName = attribName.substring(1);
                                 //System.out.println("Setting attribute @" + attribName + " to " + preText + textValue);
                                 el.setAttribute(attribName,  preText + textValue);
                                 /*if (context.startsWith("@"))  //you are looking at a named node
                                     el.setAttribute(attribName.substring(1), preText + textValue);
                                 else //you are looking at an attribute placed there with a $ or #
                                 {
                                     context = lst.get(tokenIdx - 2).getToken().substring(1);
                                     int nodeNo = Integer.parseInt(context);
                                     Attr attribNode = (Attr) g.get(nodeNo)[0];
                                     el.setAttribute(attribNode.getName(),  preText + textValue);
                                 }*/
                             }
                             else if (refNode.getNodeType() == Node.ELEMENT_NODE)  //creating an element from text
                             {
                                 newNode = xd.createElementNS(branch.getNamespaceURI(), textValue);
                                 replaceMe = true;
                             }
                             else  //a text or CDATA node
                                 refNode.setTextContent(refNode.getTextContent() + textValue);
                         }
                         else
                         {
                         	//Note: ancestry is not currently preserved
                         	Node[] nodes = g.get(groupNo);
                         	if(nodes.length > 1)
                         	{
                         		//insert the first node here
                         		newNode = selNode.cloneNode(false);
                         		switch(replaceDir)
                         		{
                         		case '<':   refNode.getParentNode().insertBefore(newNode, refNode);  break;
                         		case '>':   XmlUtils.insertAfter(refNode.getParentNode(), newNode, refNode);  break;
                         		case '\\':  refNode.appendChild(newNode);  break;
                         		}
                         		refNode = newNode;
                         		for(int n = 1; n < nodes.length - 1; n++)
                         		{
                         			newNode = nodes[n].cloneNode(false);
                         			XmlUtils.insertAfter(refNode.getParentNode(), newNode, refNode);
                         			refNode = newNode;
                         		}
                         		replaceDir = '>';
                         		newNode = nodes[nodes.length - 1].cloneNode(false);
                         		replaceMe = true;
                         	}
                         	else
                         	{
                         		newNode = selNode.cloneNode(false);
                         		replaceMe = true;
                         	}
                         }
                         //}
                     }
                     else if (str.startsWith("&"))
                     {
                         str = str.substring(1);
                         int groupNo = Integer.parseInt(str);
                         //insert loop here
                         int groupLength = g.get(groupNo).length;
                         for(int n = 0; n < groupLength; n++)
                         {
                         	Node oldNode = g.get(groupNo)[n];
                         	newNode = g.get(groupNo)[n].cloneNode(true);
 
                         	if(clonedParents.containsKey(oldNode))  //if we need to maintain insertion point data
                         	{
                         		clonedParents.get(oldNode).add(newNode);
                         		insertionNodes.put(newNode, new Vector<XmlNodeEntry>());
                         		//int prev = -5;
                         		for (int item : insertionPoints.get(oldNode))
                         		{
                         			Node nde = null;
                         			char dir = '<';
                         			if (item >= newNode.getChildNodes().getLength())
                         			{
                         				nde = newNode.getLastChild();
                         				dir = '>';
                         				if (nde == null)
                         				{
                         					nde = newNode;
                         					dir = '\\';
                         				}
                         			}
                         			else
                         				nde = newNode.getChildNodes().item(item);
                         			insertionNodes.get(newNode).add(new XmlNodeEntry(nde, dir));
 
                         			/*if (item == (prev + 1) && insertionNodes[newNode].Count > 1)
                                 {
 
 
                                 }
                                 prev = item;*/
                         		}
                         	}
                         	//place node here
                         	if(n < (groupLength - 1))
                         	{
                         		switch(replaceDir)
                         		{
                         		case '<':   refNode.getParentNode().insertBefore(newNode, refNode);  break;
                         		case '>':   XmlUtils.insertAfter(refNode.getParentNode(), newNode, refNode);  break;
                         		case '\\':  refNode.appendChild(newNode);  break;
                         		}
                         		refNode = newNode;
                         		replaceDir = '>';
                         	}
                         }
                         
                     }
                     else if(str.startsWith("^"))
                     {
                         str = str.substring(1);
                         int groupNo = Integer.parseInt(str);
                         Enumeration<Node> ecl = clonedParents.keys();
                         while(ecl.hasMoreElements())
                         {
                         	Node oldParent = ecl.nextElement();
                             if (clonedParents.get(oldParent).contains(refNode))
                             {
                                 int childNo = oldAncestry.get(oldParent).indexOf(groupNo);
                                 if (childNo != -1)
                                 {
                                     replaceDir = insertionNodes.get(refNode).get(childNo).getDir();
                                     refNode = insertionNodes.get(refNode).get(childNo).getNode();
                                 }
                                 break;
                             }
                         }
                         continue;
                     }
                     else if (str.startsWith("%"))
                     {
                         str = str.substring(1);
                         int groupNo = Integer.parseInt(str);
                         Node xn = g.get(groupNo)[0];
                         Node insertPoint;
                         if (refNode != null)
                             insertPoint = refNode;
                         else
                             insertPoint = branch.getParentNode();
                         Node par = insertPoint;
                         if (replaceDir == '<' || replaceDir == '>')
                             par = insertPoint.getParentNode();
                         if (xn.hasChildNodes())
                         {
                             if (par.getNodeType() == Node.CDATA_SECTION_NODE)
                             {
                                 String append = "";
                                 NodeList childNodeList = xn.getChildNodes();
                                 for (int l = 0; l < childNodeList.getLength(); l++)
                                 {
                                 	Node cn = childNodeList.item(l);
                                     append += XmlUtils.outerXml(cn);//append += cn.OuterXml;
                                 }
                                 CDATASection sec = (CDATASection)par;
                                 if (replaceDir == '<')
                                     sec.setTextContent(append + sec.getTextContent());
                                 else
                                     sec.setTextContent(sec.getTextContent() + append);// += append;
                             }
                             else
                             {
                                 Node first = xn.getChildNodes().item(0).cloneNode(true);
                                 
                                 
                                 //first.ParentNode.RemoveChild(first);
 
                                 switch (replaceDir)
                                 {
                                     case '<': par.insertBefore(first, insertPoint); break;
                                     case '>': XmlUtils.insertAfter(par, first, insertPoint); break;//par.insertAfter(first, insertPoint); break;
                                     default:
                                         //if (insertionPoint > -1)
                                             //par.InsertBefore(first, insertPoint.ParentNode.ChildNodes[insertionPoint]);
                                         //else
                                         par.appendChild(first); 
                                         break;
                                 }
                                 insertPoint = first;
                                 newNode = first;
                                 for (int i = 1; i < xn.getChildNodes().getLength(); i++)
                                 {
                                     Node nn = xn.getChildNodes().item(i).cloneNode(true);
                                     //nn.ParentNode.RemoveChild(nn);
                                     XmlUtils.insertAfter(par, nn, insertPoint);//par.insertAfter(nn, insertPoint);
                                     insertPoint = nn;
                                     newNode = nn;
                                 }
                             }
                             //if (newNode.ParentNode == xn)
                             //Console.WriteLine("WTF");
                         }
                         if (refNode == null)
                         {
                             par.removeChild(branch);
                         }
                         replaceMe = false;
                         /*String s1 = xn.InnerXml;
                         String s2 = refNode.InnerXml;
                         if (!s1.Equals(s2))
                         {
                             Console.WriteLine(s1 + "\n------------------\n" + s2 + "\n========================\n");
                              Console.ReadKey();
                         }*/
                     }
                     else if (str.startsWith("\""))
                     {
                         str = str.substring(1);
                         int paramVal = 0;
                         //if (paramList != null && Int32.TryParse(str, out paramVal))
                         if(paramList != null)
                         {
                         	try
                         	{
                         		paramVal = Integer.parseInt(str);
                         		str = paramList[paramVal];
                         	}
                         	catch(NumberFormatException ex)
                         	{}
                         }
                         newNode = xd.createTextNode(str);
                     }
                     else if (str.startsWith("@"))
                     {
                         str = str.substring(1);
                         String[] kvp = str.split("=");
                         replaceMe = false;
                         int paramVal = 0;
 
                         String key = kvp[0];
                         //if (paramList != null && Int32.TryParse(key, out paramVal))
                         if(paramList != null)
                         {
                         	try
                         	{
                         		paramVal = Integer.parseInt(str);
                         		key = paramList[paramVal];
                         	}
                         	catch(NumberFormatException ex)
                         	{}
                         }
                             //key = paramList[paramVal];
                         String value = "";
                         if (kvp.length == 2)
                         {
                             value = kvp[1];
                             //if (paramList != null && Int32.TryParse(value, out paramVal))
                             if(paramList != null)
                             {
                             	try
                             	{
                             		paramVal = Integer.parseInt(value);
                             		value = paramList[paramVal];
                             	}
                             	catch(Exception ex)
                             	{}
                             }
                         }
                         if (!key.equals(""))  //key could be blank if you are copying a grabbed attribute node
                         {
                             //Attr newAttrib = xd.createAttribute(key);
                             //newAttrib.setValue(value);
                             Element el = (Element) refNode;
                             el.setAttribute(key, value);
                         }
                     }
                     else
                     {
                         int paramVal = 0;
                         if(paramList != null)
                         {
                         	try
                         	{
                         		paramVal = Integer.parseInt(str);
                         		str = paramList[paramVal];
                         	}
                         	catch(Exception ex)
                         	{}
                         }
                         //if (paramList != null && Int32.TryParse(str, out paramVal))
                             
                         if (str.equals("CDATA"))
                             newNode = xd.createCDATASection("");
                         else
                             newNode = xd.createElementNS(branch.getNamespaceURI(), str);
                     }
                     if (replaceMe && refNode != null)
                     {
                         if (refNode.getNodeType() == Node.CDATA_SECTION_NODE)
                         {
                             CDATASection sec = (CDATASection) refNode;
                             if (replaceDir == '<')
                                 //sec.setTextContent(newNode.OuterXml + sec.getTextContent());
                             	sec.setTextContent(XmlUtils.outerXml(newNode) + sec.getTextContent());
                             else
                                 sec.setTextContent(sec.getTextContent() + XmlUtils.outerXml(newNode));
                         }
                         else
                         {
                             switch (replaceDir)
                             {
                                 case '\\':// refNode.AppendChild(newNode); break;
                                     //if (insertionPoint > -1)
                                     //    refNode.InsertBefore(newNode, refNode.ParentNode.ChildNodes[insertionPoint]);
                                     //else
                                     refNode.appendChild(newNode);
                                     break;
                                 case '>': XmlUtils.insertAfter(refNode.getParentNode(), newNode, refNode); break;//refNode.getParentNode().InsertAfter(newNode, refNode); break;
                                 case '<': refNode.getParentNode().insertBefore(newNode, refNode); break;
                             }
                         }
                     }
                     replaceDir = '0';
                     //insertionPoint = -1;
                     if (topNode == null)
                     {
                         topNode = newNode;
                         Node src = branch.getParentNode();
                         
                         
                         if (topNode != null && replaceMe)
                         {
                             if (src.getNodeType() != Node.DOCUMENT_NODE)
                                 src.replaceChild(topNode, branch);
                             else
                                 xd.replaceChild(topNode, xd.getDocumentElement());
                             if (branch == this.xd)
                                 this.xd = topNode;
                         }
                     }
                     refNode = newNode;
                 }
 
             }
 
             //cull other nodes in the original pattern that are ajacent to the topNode (this is an incomplete solution)
             //Already done
             /*foreach (XmlNode nde in g.get())
             {
                 if (nde.ParentNode != null)
                 {
                     nde.ParentNode.RemoveChild(nde);
                 }
             }*/
 
             //String xxx = topNode.ParentNode.OuterXml;
             //else
             //xd.DocumentElement = topNode;
 
             return topNode;
         }
 
         public void transform(String[] findPatterns, String[] replacePatterns, String[] paramList)
         {
             for (int i = 0; i < findPatterns.length; i++)
             {
                 replaceAll(findPatterns[i], replacePatterns[i], paramList);
             }
         }
  
     }
 
     class XWalker
     {
         Node nde;
         ELinkedList<XTreeNode> tokenList;
         String[] paramStrings;
 
         Node currTree;
         //LinkedListNode<XTreeNode> currItem;
 
         boolean stop = false;
         NodeGroup eg = null;
 
         int matchNo = 0;
         int lastMatch = 1;
 
         int debug = 0;
         int options = 0;
         
         //flags
         boolean ignoreComments;
         boolean ignoreEmptyText;
         boolean ignoreCase;
         //String debugText;// = "";
         
         
 
         public XWalker(Node x, ELinkedList<XTreeNode> l, String[] inParams)
         {
             nde = x;
             tokenList = l;
             paramStrings = inParams;
             //currTree = xd.FirstChild;
             currTree = nde;
             //currItem = lst.First;
             stop = false;
            // debugText = "";
             ignoreComments = false;
             ignoreEmptyText = false;
             ignoreCase = false;
         }
         
         public void setOption(int input)
         {
         	switch(input)
         	{
         	case XPattern.IGNORE_COMMENTS:
         		ignoreComments = true; break;
         	case XPattern.IGNORE_EMPTY_TEXT:
         		ignoreEmptyText = true; break;
         	case XPattern.IGNORE_CASE:
         		ignoreCase = true; break;
         	}
         }
 
         public XWalker(Document x, ELinkedList<XTreeNode> l, int lastMatch, String[] ip)
         {
         	this(x, l, ip);
             setLastMatch(lastMatch);
         }
 
         public void setLastMatch(int x)
         {
             if (x < 1)
                 x = 1;
             lastMatch = x;
             eg = null;
         }
 
         public void start()
         {
         	//ListIterator li = lst.listIterator(0);
             walk(0, new ELinkedList[tokenList.size()]);
         }
 
         public void reset()
         {
 
             currTree = nde;
             matchNo = 0;
             lastMatch = 1;
         }
 
         private void setCurrentTree(Node t)
         {
             if (stop == true)
                 return;
             currTree = t;
         }
 
         public ELinkedList<XTreeNode> getList()
         {
             return tokenList;
         }
 
         public Node getCurrentTree()
         {
             return currTree;
         }
         
         public boolean isMatch()
         {
         	for (XTreeNode item : tokenList)
         	{
         		if(item.getMatchList().size() == 0 && item.getEmptyOK() == false)
         			return false;
         	}
         	return true;
         }
         
         public void resetMatch()
         {
         	for(XTreeNode item:tokenList)
         	{
         		item.clearMatch();
         	}
         }
         
         private int getParentIndex(Node parentNode, Node childNode/*, int expectedStart*/)
         {
         	Node child = childNode;
         	while(child.getParentNode() != parentNode)
         	{
         		child = child.getParentNode();
         		if(child == null)
         			return Integer.MAX_VALUE - 1;
         	}
         	int len = parentNode.getChildNodes().getLength();
         	if(stop)
         		return len;
         	for(int i = 0; i < len; i++)
         		if(parentNode.getChildNodes().item(i) == child)
         			return i;
         	return Integer.MAX_VALUE;
         }
         
         public void printTokenList()
         {
         	System.out.println("==TokenList==========================");
         	for(XTreeNode item:tokenList)
         	{
         		String out = item.token;// + "||EmptyOK=" + item.getEmptyOK() + "||" + item.getMatchList().size();
         		System.out.println(out);
         	}
         	System.out.println("=================================");
         }
         
         private char getLastDirection(int input)
         {
         	for(int i = input; i >= 0; i--)
         	{
         		String dir = tokenList.get(i).getToken();
         		if(dir.equals(">") || dir.equals("<") || dir.equals("\\"))
         			return dir.charAt(0);
         	}
         	return '0';
         }
 
         /*REFACTOR NOTES: Store the XmlNodes you find in the corresponding XTreeNode so that you don't need to match them up later;
          the XTreeNode already stores the list of groups the node needs to belong to so just storing it with the XTreeNode would
          be a better idea than the unwind later*/
         private void walk(int findMe, ELinkedList<Node>[] inPath/*, Node refNode*/)  //refNode will be null if you are doing a normal find pass, 
         {
         	if(findMe < tokenList.size())
         	{
         		boolean ignoreMe = false;
         		if(currTree.getNodeType() == Node.COMMENT_NODE && ignoreComments)
         			ignoreMe = true;
         		if(currTree.getNodeType() == Node.TEXT_NODE && ignoreEmptyText)
         		{
         			String value = currTree.getTextContent();
         			if(value.trim().length() == 0)
         				ignoreMe = true;
         		}
         		if(ignoreMe)
         		{
         			char ld = getLastDirection(findMe);
         			switch(ld)
         			{
         			case '<':
         				if(currTree.getPreviousSibling() != null)
         				{
         					setCurrentTree(currTree.getPreviousSibling());
         					walk(findMe, inPath);
         				}
         				return;
         			case '>':
         				if(currTree.getNextSibling() != null)
         				{
         					setCurrentTree(currTree.getNextSibling());
         					walk(findMe, inPath);
         				}
         				return;
         			default:
         				return;
         			}
         		}
         	}
         	
         	int nTokens = inPath.length;
         	//if(findMe == 0)  //because I'm lazy
         		//resetMatch();
             ELinkedList<Node>[] path = new ELinkedList[nTokens];
             if(findMe > 0)
             {
             	for(int i = 0; i < nTokens; i++)
             	{
             		//System.out.println(inPath[i].size());
             		path[i] = new ELinkedList<Node>();
             		for (Node item : inPath[i])
             			path[i].addLast(item);
             	}
             }
             else
             {
             	for(int i = 0; i < nTokens; i++)
             		path[i] = new ELinkedList<Node>();
             }
             if(stop == true)
             {
                 
                 return;
             }
             //String findAttrib = null;
             //if(findMe >= tokenList.size())
             	//printTokenList();
             if(findMe >= tokenList.size() /*&& isMatch()*/)//if (lst.get(findMe) == null)
             {
                // if (debug >= 1)
                    // debugText += ", done";
                 matchNo++;
                 if (lastMatch == matchNo)
                 {
                     //Node last = path.getLast();
                     /*if (currTree != last)        //note: this statement allows for ending a find operation with a direction
                         path.AddLast(currTree); */ //I may remove it if I find the last node to be one too many (Removed - the * is there for a reason)
                     eg = new NodeGroup(this, path);
                     stop = true;
                     lastMatch++;
                 }
                 else if(currTree.hasChildNodes())
                 {
                     Node parentTree = currTree;
                     //debugText += ", restarted";
                     for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                     {
                         setCurrentTree(parentTree.getChildNodes().item(i));
                         
                         walk(0, new ELinkedList[nTokens]);
                     }
                 }
                 return;
             }
             
             String str = tokenList.get(findMe).getToken();
             int nextNode = 0;
             
             char c = str.charAt(0);
 
             switch (c)
             {
                 case '/':
                     if(currTree.getParentNode() != null)
                     {
                         //path.AddLast(currTree.ParentNode);
                         setCurrentTree(currTree.getParentNode());
                         //currItem = currItem.Next;
                         
                         walk(findMe + 1, path);
                     }
                     return;        
                 case '\\':
                     //currItem = currItem.Next;
                     Node parentTree = currTree;
                     boolean nextWithout = false;
                     NodeList allChildren = parentTree.getChildNodes();
                     String lookahead = tokenList.get(findMe + 1).getToken();
                     if(lookahead.startsWith("-"))
                     	nextWithout = true;
                     for(int i = 0; i < allChildren.getLength(); i++)
                     {
                         //path.AddLast(parentTree.ChildNodes.Item(i));  
                         setCurrentTree(allChildren.item(i));
                         walk(findMe + 1, path);
                         //find where currentNode is
                         int loc = getParentIndex(parentTree, currTree);
                         if(loc < allChildren.getLength())
                         	i = loc;
                         //while(allChildren.item(i) != currTree)
                         	//i++;
                         if(nextWithout && tokenList.get(findMe).getEmptyOK() == false)
                         {
                         	
                         	tokenList.get(findMe).setEmptyOK(true);
                         	//setCurrentTree(parentTree);
                         	for(int j = 0; j < allChildren.getLength(); j++)
                         	{
                         		setCurrentTree(allChildren.item(j));
                         		walk(0, new ELinkedList[nTokens]);
                         	}
                         	return;
                         }
                         //path.RemoveLast();
                     }
                     if(nextWithout && tokenList.get(findMe).getEmptyOK() == true)
                     {
                     	  //make a without statement that starts with -
                     	nextNode = nodeEnd(findMe + 1) + 1;
                     	walk(nextNode, path);
                     }
                     //empty lookahead condition
                     if(allChildren.getLength() == 0)
                     {
                     	if(lookahead.length() > 1 && (lookahead.endsWith("?") || lookahead.endsWith("*")))
                     	{
                     		nextNode = nodeEnd(findMe + 1) + 1;
                     		walk(nextNode, path);
                     	}
                     }
                     return;
                 case '<':
                 	//nextWithout = false;
                 	int currLoc = 0;
                 	NodeList childList = currTree.getParentNode().getChildNodes();
                 	while(true)
                 	{
                 		if(childList.item(currLoc) == currTree)
                 			break;
                 		currLoc++;
                 	}
                     if(tokenList.get(findMe + 1).getToken().startsWith("-"))
                     {
                     	//nextWithout = true;
                     	
                     	for(int j = currLoc - 1; j >= 0; j--)
                     	{
                     		setCurrentTree(childList.item(j));
                     		walk(findMe + 1, path);
                     		if(tokenList.get(findMe).getEmptyOK() == false)
                             {
                             	tokenList.get(findMe).setEmptyOK(true);
                             	
                             	parentTree = currTree;
                                 // debugText += ",failed\n";
                                 for (int i = currLoc; i < parentTree.getChildNodes().getLength(); i++)
                                 {
                                     setCurrentTree(parentTree.getChildNodes().item(i));
                                     walk(0, new ELinkedList[nTokens]);
                                 }
                                 
                             	return;
                             }
                     	}
                     	if(tokenList.get(findMe).getEmptyOK() == true)
                     	{
                     		path[findMe + 1].add(currTree);
                     		nextNode = nodeEnd(findMe + 1) + 1;
                         	walk(nextNode, path);
                     	}
                     }
                     if (currTree.getPreviousSibling() != null)
                     {
                         setCurrentTree(currTree.getPreviousSibling());
                         walk(findMe + 1, path);
                     }
                     else
                     {
                         //currItem = lst.First;
                         parentTree = currTree;
                         // debugText += ",failed\n";
                         for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                         {
                             setCurrentTree(parentTree.getChildNodes().item(i));
                             walk(0, new ELinkedList[nTokens]);
                         }
                     }
                     return;
                 case '>':
                 	//nextWithout = false;
                 	currLoc = 0;
                 	childList = currTree.getParentNode().getChildNodes();
                 	while(true)
                 	{
                 		if(childList.item(currLoc) == currTree)
                 			break;
                 		currLoc++;
                 	}
                     if(tokenList.get(findMe + 1).getToken().startsWith("-"))
                     {
                     	//nextWithout = true;
                     	for(int j = currLoc + 1; j < childList.getLength(); j++)
                     	{
                     		setCurrentTree(childList.item(j));
                     		walk(findMe + 1, path);
                     		if(tokenList.get(findMe).getEmptyOK() == false)
                             {
                             	tokenList.get(findMe).setEmptyOK(true);
                             	
                             	parentTree = currTree;
                                 // debugText += ",failed\n";
                                 for (int i = currLoc; i < parentTree.getChildNodes().getLength(); i++)
                                 {
                                     setCurrentTree(parentTree.getChildNodes().item(i));
                                     walk(0, new ELinkedList[nTokens]);
                                 }
                                 
                             	return;
                             }
                     	}
                     	if(tokenList.get(findMe).getEmptyOK() == true)
                     	{
                     		path[findMe + 1].add(currTree);
                     		nextNode = nodeEnd(findMe + 1) + 1;
                         	walk(nextNode, path);
                     	}
                     }
                     if (currTree.getNextSibling() != null)
                     {
                         setCurrentTree(currTree.getNextSibling());
                         walk(findMe + 1, path);
                     }
                     else
                     {
                         //currItem = lst.First;
                         parentTree = currTree;
                         // debugText += ",failed\n";
                         for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                         {
                             setCurrentTree(parentTree.getChildNodes().item(i));
                             walk(0, new ELinkedList[nTokens]);
                         }
                     }
                     return;
                 case '@':
                     if(currTree.getNodeType() == Node.ELEMENT_NODE)
                     {
                     	int end = nodeEnd(findMe);
                     	boolean passes = false;
                     	if(matchAttributes(currTree, findMe, end, path) > -1)
                     		passes = true;
                     	
                     	parentTree = currTree;
                     	for(int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                     	{
                     		setCurrentTree(parentTree.getChildNodes().item(i));
                     		if(passes)
                     			walk(end + 1, path);
                     		else
                     			walk(0, new ELinkedList[nTokens]);
                     	}
                     }
                     break;
                 case '\"':  //possibly pull this case out of the directional switch
                     if (currTree.getNodeType() != Node.TEXT_NODE)
                     {
                         Node parentTree2 = currTree;
                         for (int i = 0; i < parentTree2.getChildNodes().getLength(); i++)
                         {
                             setCurrentTree(parentTree2.getChildNodes().item(i));
                             walk(0, new ELinkedList[nTokens]);
                         }
                         return;
                     }
                     else
                     {
                         //bool useRegex = false;
                         boolean negated = false;
                         boolean perhaps = false;
                         XTreeNode xn = tokenList.get(findMe + 1);
                         if(xn == null)  //match any text node
                         {
                             path[findMe].addLast(currTree);
                             tokenList.get(findMe).addToMatch(currTree);
                             walk(findMe + 1, path);
                             return;
                         }
                         String findText = xn.getToken();
                         if (findText.startsWith("!"))
                         {
                             findText = findText.substring(1);
                             negated = true;
                         }
                         if (findText.endsWith("?"))
                         {
                             findText = findText.substring(0, findText.length() - 1);
                             perhaps = true;
                         }
                         /*if (findText.StartsWith("/"))
                         {
                             useRegex = true;
                             //xn = xn.Next;
                         }*/
                         MatchCollection mc = new MatchCollection();
                         boolean passes = matchString(findText, currTree.getTextContent(), mc);
                         if (passes ^ negated)
                         {
                         	Matcher matcher = mc.getMatcher();
                         	tokenList.get(findMe).addToMatch(currTree);
                             path[findMe].addLast(currTree);
                             if (matcher != null)
                             {
                                 //int kk = 0;
                                 //matcher.groupCount()
                                 for(int kk = 0; kk < matcher.groupCount() + 1; kk++)
                                 {
                                     xn.setRegexMatchNo(kk);  //REFACTOR NOTES: instead of just storing the index, go ahead and calculate the XmlNode[][] you get from this
                                     walk(findMe + 2, path);
                                 }
                             }
                             else
                                 walk(findMe + 2, path);
                         }
                         else if (perhaps)
                         {
                             path[findMe].addLast(null);
                             tokenList.get(findMe).addToMatch(null);
                             walk(findMe + 2, path);
                         }
                         return;
                     }
                 /*case '*':
                     path[findMe].addLast(currTree);
                     tokenList.get(findMe).addToMatch(currTree);
                     walk(findMe + 1, path);
                     return;*/
                     //break;
                 default:
                 	boolean negated = false;
                     boolean passes = false;
                     boolean perhaps = false;
                     boolean multi = false;
                     boolean escaped = false;
                     boolean without = false;
                     
                     String fullToken = str;
                     if (str.startsWith("!") && !str.equals("!--"))
                     {
                         negated = true;
                         str = str.substring(1);
                     }
                     if(str.startsWith("-"))
                     {
                     	without = true;
                     	str = str.substring(1);
                     }
                     if(str.length() > 1)
                     {
                     	if (str.endsWith("?") || str.endsWith("*"))
                     		perhaps = true;
                     	//str = str.substring(0, str.length() - 1);
 
                     	if(str.endsWith("*") || str.endsWith("+"))
                     		multi = true;
 
                     	if(perhaps || multi)
                     		str = str.substring(0, str.length() -1);
                     }
                     nextNode = nodeEnd(findMe) + 1;
                     if(multi)
                     {
                     	if(nextNode < tokenList.size())
                     	{
                     		
                     		//Node current = currTree;
                     		Node currNext = currTree.getNextSibling();
                     		
                     		while( currNext != null)
                     		{
                     			
                     			walk(nextNode, path);  //findMe + 2 is the escape sequence
                     			//if the current node matches the current pattern
                     			if(stop == true) // we could have found a full match pattern here
                     				return;
                     			if(singleNodeMatch(str, currTree, findMe, path) > 0)
                     				path[findMe].add(currTree);
                     			setCurrentTree(currNext);
                     			currNext = currNext.getNextSibling();
                     		}
                     		walk(nextNode, path);  //findMe + 2 is the escape sequence
                 			//if the current node matches the current pattern
                 			if(stop == true) // we could have found a full match pattern here
                 				return;
                 			if(singleNodeMatch(str, currTree, findMe, path) > 0)
                 				path[findMe].add(currTree);
                     	}
                     	else
                     	{
                     		//just grab all nodes that match the filter
                     		Node currNext = currTree.getNextSibling();
                     		while(currNext != null)
                     		{
                     			if(singleNodeMatch(str, currTree, findMe, path) > 0)
                     				path[findMe].add(currTree);
                     			//else
                     				//break;
                     			setCurrentTree(currNext);
                     			currNext = currNext.getNextSibling();
                     		}
                     		if(singleNodeMatch(str, currTree, findMe, path) > 0)
                 				path[findMe].add(currTree);
                     	}
                     	if(path[findMe].size() == 0 && !perhaps)
                 			walk(0, new ELinkedList[nTokens]);  //fail
                 		else
                 			walk(nextNode, path);  //this will create the NodeGroup
                     }
                     else
                     {
                     	//System.out.println(findMe);
                     	int match = singleNodeMatch(str, currTree, findMe, path) + 1;
                     	//System.out.println(findMe + " next:" + nextNode);
                     	//if(without)   
                     		//return;
                     	if((match > 0) ^ negated)
                     	{	
                     		//System.out.println(currTree.getNodeName() + " matched " + fullToken + " (going to token " + nextNode + ")");
                     		path[findMe].addLast(currTree);
                             //tokenList.get(findMe).addToMatch(currTree);
                     		if(!without)
                     			walk(nextNode, path);
                     		else
                     			tokenList.get(findMe - 1).setEmptyOK(false);
                         }
                     	else if(without)
                     		return;
                     	else
                         {
                             //System.out.println(currTree.getNodeName() + " didn't match " + fullToken + "(going to 0)");
                             Node elParentTree = currTree;
                             
                             for (int i = 0; i < elParentTree.getChildNodes().getLength(); i++)
                             {
                                 setCurrentTree(elParentTree.getChildNodes().item(i));
                                 walk(0, new ELinkedList[nTokens]);
                             }
                         }	
                     }
             }
             /*if (findAttrib != null)
             {
                 if (currTree.getNodeType() != Node.ELEMENT_NODE)
                     return;
                 boolean negated = false;
                 if (findAttrib.startsWith("!"))
                 {
                     negated = true;
                     findAttrib = findAttrib.substring(1);
                 }
                 boolean perhaps = false;
                 if (findAttrib.endsWith("?"))
                 {
                     perhaps = true;
                     findAttrib = findAttrib.substring(0, findAttrib.length() - 1);
                 }
                 Element el = (Element)currTree;
                 if (el.getAttributeNode(findAttrib) != null)
                 {
                     XTreeNode xn = tokenList.get(findMe + 2);
                     String postAttrib = "";
                     if (xn != null)
                         postAttrib = xn.getToken();
                     if (postAttrib.equals("="))
                     {
                         //String findVal = xn.Next.Value.getToken();
                         Attr attrib = el.getAttributeNode(findAttrib);
                         String attribVal = attrib.getValue();
                         //bool useRegex = false;
                         xn = tokenList.get(findMe + 3);
                         String searchVal = xn.getToken();
                         boolean valueNegated = false;
                         if (searchVal.startsWith("!"))
                         {
                             valueNegated = true;
                             searchVal = searchVal.substring(1);
                         }
                         boolean valuePerhaps = false;
                         if (searchVal.endsWith("?"))
                         {
                             valuePerhaps = true;
                             searchVal = searchVal.substring(0, searchVal.length() - 1);
                         }
                         //if (searchVal.Equals("/"))
                         {
                             useRegex = true;
                             xn = xn.Next;
                         }//
                         MatchCollection mc = new MatchCollection();
                         boolean passes = matchString(searchVal, attribVal, mc);
                         if (passes ^ valueNegated)  //you found the attribute with the matched value OR a negated value
                         {
                             path[findMe + 1].addLast(currTree.getAttributes().getNamedItem(findAttrib));
                             tokenList.get(findMe + 1).addToMatch(currTree.getAttributes().getNamedItem(findAttrib));
                             Matcher matcher = mc.getMatcher();
                             if (matcher != null)
                             {
                                 //int kk = 0;
                             	//Matcher matcher = mc.getMatcher();
                                 for(int kk = 0; kk < matcher.groupCount(); kk++)
                                 {
                                     xn.setRegexMatchNo(kk);
                                     walk(findMe + 4 + kk, path);
                                 }
                             }
                             else
                                 walk(findMe + 4, path);
                         }
                         else if (valuePerhaps)  //the attribute's value didn't match
                         {
                             path[findMe + 1].addLast(null);
                             tokenList.get(findMe + 1).addToMatch(null);
                             walk(findMe + 4, path);
                         }
                         else
                         {
                             Node parentTree = currTree;
                             for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                             {
                                 setCurrentTree(parentTree.getChildNodes().item(i));
                                 walk(0, new ELinkedList[nTokens]);
                             }
                         }
                         return;
                     }
                     else if (!negated)  //you were looking for just the attribute, and you found it
                     {
                         path[findMe + 1].addLast(currTree.getAttributes().getNamedItem(findAttrib));
                         tokenList.get(findMe + 1).addToMatch(currTree.getAttributes().getNamedItem(findAttrib));
                         walk(findMe + 4, path);
                         return;
                     }
                     else
                     {
                         Node parentTree = currTree;
                         for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                         {
                             setCurrentTree(parentTree.getChildNodes().item(i));
                             walk(0, new ELinkedList[nTokens]);
                         }
                         return;
                     }
                 }
                 else if (negated || perhaps)  //you didn't find the attribute
                 {
                     path[findMe].addLast(null);
                     tokenList.get(findMe + 1).addToMatch(null);
                     findMe += 2;//findMe.Next.Next;
                     if (tokenList.get(findMe) != null && tokenList.get(findMe).getToken().equals("="))
                         findMe += 2;// findMe.Next.Next;
                     walk(findMe, path);
                     return;
                 }
                 //else if (findAttrib.Equals("*"))
                 {
 
                 }//
                 else
                 {
                     Node parentTree = currTree;
                     for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                     {
                         setCurrentTree(parentTree.getChildNodes().item(i));
                         walk(0, new ELinkedList[nTokens]);
                     }
                     return;
                 }
             }
             else*/
             /*{
                 
                 
                 
                 	//escaped = true;
                 
                 if (str.equals("CDATA"))
                 {
                     //debugText += "looking for " + str + "\n";
                     
                     if (currTree.getNodeType() == Node.CDATA_SECTION_NODE)
                         passes = true;
                     
                     if(passes ^ negated)
                     {
                         path[findMe].addLast(currTree);
                         tokenList.get(findMe).addToMatch(currTree);
                         walk(findMe + 1, path);
                     }
                     else if (perhaps)
                     {
                         path[findMe].addLast(null);
                         tokenList.get(findMe).addToMatch(null);
                         walk(findMe + 1, path);
                     }
                     else
                     {
                         Node parentTree = currTree;
                         for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                         {
                             setCurrentTree(parentTree.getChildNodes().item(i));
                             walk(0, new ELinkedList[nTokens]);
                         }
                     }
                 }
                 else
                 {
                     String elemName = "";
                     if (currTree.getNodeType() == Node.ELEMENT_NODE)
                     {    //return;
 
                         Element el = (Element)currTree;
                         elemName = el.getNodeName();
                     }
                     //debugText += "looking for " + str + "   ";
                     //debugText += "found " + el.Name + "\n";
                     if (elemName.equals(str))
                         passes = true;
                     if (passes ^ negated)
                     {
                         path[findMe].addLast(currTree);
                         tokenList.get(findMe).addToMatch(currTree);
                         walk(findMe + 1, path);
                     }
                     else if (perhaps)
                     {
                         path[findMe].addLast(null);
                         tokenList.get(findMe).addToMatch(null);
                         walk(findMe + 1, path);
                     }
                     else
                     {
                         //currItem = lst.First;
                         Node parentTree = currTree;
                         // debugText += ",failed\n";
                         for (int i = 0; i < parentTree.getChildNodes().getLength(); i++)
                         {
                             setCurrentTree(parentTree.getChildNodes().item(i));
                             walk(0, new ELinkedList[nTokens]);
                         }
                     }
                 }
             }*/
             
         }
         
         private int nodeEnd(int nodeStart)
         {
         	for(int i = nodeStart + 1; i < tokenList.size(); i++)
         	{
         		String str = tokenList.get(i).getToken();
         		switch(str.charAt(0))
         		{
         		case '/': case '\\': case '>': case '<':
         			return i - 1;
         		}
         	}
         	return tokenList.size() - 1;
         }
         
         private int singleNodeMatch(String toMatch, Node input, int attribStart, ELinkedList<Node>[] path)
         {
         	if(toMatch.startsWith("\""))
         	{
         		return attribStart;
         	}
         	//System.out.println("Looking for:" + toMatch + " input:" + input.getNodeName());
         	if(toMatch.equals("!--") && input.getNodeType() == Node.COMMENT_NODE)
         		return attribStart;
         	if(toMatch.equals("CDATA") && input.getNodeType() == Node.CDATA_SECTION_NODE)
         		return attribStart;
         	if(toMatch.equals(input.getNodeName()) || toMatch.equals("*"))
         	{
         		int attribEnd = nodeEnd(attribStart);
         		if(attribEnd > attribStart)
         			return matchAttributes(input, attribStart + 1, attribEnd, path);
         		else
         			return attribStart;
         	}
         	return -1;
         }
         
         private int matchAttributes(Node input, int attribStart, int attribEnd, ELinkedList<Node>[] path)
         {
         	boolean attribFilter = false;
         	boolean multi = false;
         	boolean matches = false;
         	boolean perhaps = false;
         	boolean negated = false;
         	NamedNodeMap allAttribs = input.getAttributes();
         	int attribsLength = allAttribs.getLength();
         	ELinkedList<Attr> matchedAttribs = null;  //this may be a duplicate declaration
         	for(int i = attribStart; i <= attribEnd; i++)
         	{
         		//valueFilter = false;
             	multi = false;
             	matches = false;
             	perhaps = false;
             	negated = false;
         		
         		String filter = tokenList.get(i).getToken();
         		if(filter.equals("@"))
         		{
         			attribFilter = true;
         			matchedAttribs = new ELinkedList<Attr>();
         			continue;
         		}
         		else if(filter.equals("="))
         		{
         			attribFilter = false;
         			continue;
         		}
         		
         		if(filter.startsWith("!"))
         		{
         			negated = true;
         			filter = filter.substring(1);
         		}
         		
         		
         		//grab all attributes that match the filter
         		if(attribFilter)
         		{
         			//System.out.println("Finding " + filter);
         			if(filter.endsWith("*") || filter.endsWith("?"))
             			perhaps = true;
             		if(filter.endsWith("*") || filter.endsWith("+"))
             			multi = true;
             		if(multi || perhaps)
             			filter = filter.substring(0, filter.length() - 1);
             		matches = false;
             		for(int j = 0; j < attribsLength; j++)
             		{
             			Attr myAttr = (Attr) allAttribs.item(j);
             			if(filter.equals("*"))
             				matches = true;
             			//System.out.println("Comparing to " + allAttribs.item(j).getNodeName());
             			if(filter.equals(myAttr.getNodeName()))
             				matches = true;
             			if(matches ^ negated)
             			{
             				path[i].add(myAttr);
             				matchedAttribs.addLast(myAttr);
             				
             				if(!multi)
             					break;
             			}
             		}
         		}
         		
         		//eliminate all attributes that don't match the value filter
         		else
         		{
         			int j = 0;
         			while(j < matchedAttribs.size())
         			{
         				String val = matchedAttribs.get(j).getValue();
         				if(matchString(filter, val, null) ^ negated)
         					j++;
         				else
         				{
         					Attr myAttr = matchedAttribs.get(j);
         					matchedAttribs.remove(j);
         					path[i - 2].remove(myAttr);
         				}
         			}
         		}
         		if(matchedAttribs.size() == 0 && perhaps == false)
         			return -1;
         	}
         	return attribEnd;
         }
         //for java, we may need a different function here
         private boolean matchString(String toMatch, String input, MatchCollection col)
         {
             //col = null;
             boolean useRegex = false;
             if(toMatch.startsWith("/"))
             {
                 useRegex = true;
                 toMatch = toMatch.substring(1);
             }
             if (toMatch.equals("*") && !useRegex)
                 return true;
             if (paramStrings != null)
             {
                 int grabIndex = Integer.parseInt(toMatch);
                 toMatch = paramStrings[grabIndex];
             }
             
             if (useRegex)
             {
                 Pattern rg = Pattern.compile(toMatch);
                 Matcher cx = rg.matcher(input);
                 //boolean b = cx.matches();
                 col.setMatcher(cx);
                 return (cx.find(0));
             }
             else
                 return input.equals(toMatch);
            
         }
 
         public NodeGroup getEGroup()
         {
             return eg;
         }
 
         public Node[][] getRegexParts(String input, String pattern, int matchNo)
         {
             Node[][] rtnVal;
             //pattern = pattern.Substring(2);  do not do this
             if (paramStrings != null)
             {
                 pattern = paramStrings[Integer.parseInt(pattern)];
             }
             Pattern rg = Pattern.compile(pattern);
             
             Matcher m = rg.matcher(input);
             boolean b = m.find(0);
             /*Match m = rg.Matches(input)[matchNo];*/
             int groups = m.groupCount() + 1;
             rtnVal = new Node[groups][];
 
             int prevIndex = 0;
             for (int i = 0; i < groups; i++)
             {
                 rtnVal[i] = new Node[3];
                 
                 int start = input.indexOf(m.group(i), prevIndex);
                 int end = start + m.group(i).length();
                 String item = m.group(i);
                 prevIndex = end;
 
                 String[] parts = new String[3];
                 parts[0] = item;
                 parts[1] = input.substring(0, start);
                 parts[2] = input.substring(end);
 
                 for (int j = 0; j < 3; j++)
                 {
                     Attr xa = nde.getOwnerDocument().createAttributeNS(nde.getNamespaceURI(), "__String__Value__Only__Attribute__");
                     xa.setValue(parts[j]);
                     rtnVal[i][j] = xa;
                 }
             }
             return rtnVal;
         }
 
     }
 
     class XTreeNode
     {
         String token;
         ELinkedList<Integer> groupNo;
         int regexMatchNo;
         //ELinkedList<Node> match;
         boolean emptyOK;
 
         public XTreeNode(String part, ELinkedList<Integer> group)
         {
             token = part;
             if(group != null)
                 groupNo = new ELinkedList<Integer>(group);
             regexMatchNo = -1;
             //match = new ELinkedList<Node>();
             emptyOK = false;
             if(part.equals("\\") || part.equals("/") || part.equals(">") || part.equals("<") || part.equals("@"))
             	emptyOK = true;
             if(!part.startsWith("@"))
             {
             	if(part.length() > 1 && (part.endsWith("*") || part.endsWith("?")))
             		emptyOK = true;
             }
         }
         
         public String toString()
         {
         	return token;
         }
 
         public String getToken()
         {
             return token;
         }
 
         public ELinkedList<Integer> getGroupList()
         {
             return groupNo;
         }
 
         public boolean isRegex()
         {
             if (token.startsWith("/") && token.length() > 1)
                 return true;
             if (token.startsWith("@") && token.contains("=/"))
                 return true;
             return false;
         }
 
         public int getRegexMatchNo()
         {
             return regexMatchNo;
         }
 
         public void setRegexMatchNo(int n)
         {
             regexMatchNo = n;
         }
         
         public ELinkedList<Node> getMatchList()
         {
         	//return match;
         	return null;
         }
         
         public void addToMatch(Node nde)
         {
         	//match.add(nde);
         }
         
         public void clearMatch()
         {
         	//match = new ELinkedList<Node>();
         	//match.removeAll(null);
         }
         
         public void setEmptyOK(boolean val)
         {
         	emptyOK = val;
         }
         
         public boolean getEmptyOK()
         {
         	return emptyOK;
         }
     }
 
     class NodeGroup
     {
         //XmlElement[] bef;
         //XmlElement[] aft;
         Node[] match;
         Node[][] groups;
         ELinkedList<XTreeNode> lst;
         Node nde;
         int depth;
         //XmlDocument doc;  //not used until we implement $bef and $aft
 
         public NodeGroup(Node nde)
         {
             match = new Node[1];
             match[0] = nde;
             groups = new Node[1][];
             groups[0] = new Node[1];
             groups[0][0] = nde;
             depth = calcDepth(nde);
         }
         
         public NodeGroup(XWalker w, ELinkedList[] path)
         {
         	lst = new ELinkedList<XTreeNode>(w.getList());
         	nde = w.getCurrentTree();
         	
         	int max = 0;
             for (XTreeNode item : lst)  //how many groups do we have?
             {
                 ELinkedList<Integer> ags = item.getGroupList();
                 for (int grp : ags)
                 {
                     if (grp > max)
                         max = grp;
                 }
             }
             //create a list of nodes for each group
             ELinkedList<Node>[] allLists = (ELinkedList<Node>[]) Array.newInstance(ELinkedList.class, max);
             ELinkedList<Node[]> regexOverflow = new ELinkedList<Node[]>(); 
             for (int i = 0; i < max; i++)
                 allLists[i] = new ELinkedList<Node>();
             
             ELinkedList<Node> finalMatch = new ELinkedList<Node>();
             
             for(int i = 0; i < path.length; i++)
             {
             	finalMatch.addAll(path[i]);
             	
             	//attribute values
             	if(i > 0)
             	{
             		String prev = lst.get(i - 1).getToken();
             		String myText = "";
             		//attribute values
             		if(prev.equals("="))
             		{
             			Attr myAttrib = (Attr) path[i - 2].get(0);
             			
             			if(myAttrib != null)  //this could be null due to "perhaps" values
             			{
             				myText = myAttrib.getValue();
             				Attr xa = nde.getOwnerDocument().createAttribute("__String__Value__Only__Attribute__");
             				xa.setValue(myText);
             				path[i].add(xa);
             			}
             		}
             		//text values
             		else if(prev.equals("\""))
             		{
             			Text tn = (Text) path[i -1].get(0);
             			myText = tn.getTextContent();
             			Attr xa = nde.getOwnerDocument().createAttribute("__String__Value__Only__Attribute__");
         				xa.setValue(myText);
         				path[i].add(xa);
             		}
             		//regex captures 
             		if(lst.get(i).isRegex())
             		{
             			int matchNo = lst.get(i).getRegexMatchNo();
             			String pattern = lst.get(i).getToken().substring(1); //all regex matches begin with a / (that we need to remove)
             			Node[][] results = w.getRegexParts(myText, pattern, matchNo);  //I'd rather have this done somewhere else
             			path[i].removeLast();  //replace the previously added node with regex match data
             			for(int j = 0; j < results[0].length; j++)
             				path[i].add(results[0][j]);
             			for(int j = 1; j < results.length; j++)
             			{
             				regexOverflow.add(results[j]);
             			}
             		}
             	}
             	
             	//regex captures
             	
             	//text captures
             	
             	
             	for(int j = 0; j < lst.get(i).groupNo.size(); j++)
             	{
             		int listNum = lst.get(i).groupNo.get(j);
             		allLists[listNum - 1].addAll(path[i]);
             	}
             	
             	
             }
             
             int regexLen = regexOverflow.size();
             match = new Node[0];
             groups = new Node[max + regexLen][];
             match = finalMatch.toArray(match);
             //match = groups[0];
             for(int i = 0; i < max; i++)
             {
             	groups[i] = new Node[0];
             	groups[i] = allLists[i].toArray(groups[i]);
             }
             for(int i = 0; i < regexLen; i++)
             {
             	//groups[i + max] = new Node[0];
             	groups[i + max] = regexOverflow.get(i);
             }
             
             //viewNodeGroup(path);
             /*int matchLen = 0;
             for(int i = 0; i < path.length; i++)
             	matchLen += path[i].size();
             
             match = new Node[matchLen];
             int l = 0;
             for(int i = 0; i < path.length; i++)
             {
             	
             	for(int j = 0; j < path[i].size(); j++)
             	{
             		match[l] = (Node) path[i].get(j);
             		l++;
             	}
             }*/
             
             
             /*
             match = path.toArray(new Node[0]);   //populate match[0]
             //match = new XmlElement[xn.Length];
             //for (int i = 0; i < xn.Length; i++)
                 //match[i] = (XmlElement) xn[i];
             max += regexCaptures.size();
             groups = new Node[max][];
             int j = 0;
             for (ELinkedList<Node> list : allLists)
             {
                 groups[j] = list.toArray(new Node[0]);
                 j++;
             }
             for (Node[] item : regexCaptures)
             {
                 groups[j] = item;
                 j++;
             }
             depth = calcDepth(match[0]);*/
         }
         
         
         
         public void viewNodeGroup(ELinkedList[] inPath)
         {
         	System.out.println("==NodeGroup=========================");
         	for(int i = 0; i < inPath.length; i++)
         	{
         		int l1 = inPath[i].size();
         		Integer[] l2 = lst.get(i).groupNo.toArray(new Integer[0]);
         		System.out.print("(");
         		for(int j = 0; j < l2.length; j++)
         		{
         			System.out.print(l2[j] + ",");
         		}
         		System.out.print(") "+ l1 + "/");
         		
         	}
         	System.out.println();
         	System.out.println("Match length: " + match.length);
         	System.out.print("Groups length: " + groups.length + " /");
         	for(int i = 0; i < groups.length; i++)
         		System.out.print(groups[i].length + "/");
         	System.out.println();
         	System.out.println("===========================");
         }
 
         public NodeGroup(XWalker w, ELinkedList<Node> path)
         {
             lst = new ELinkedList<XTreeNode>(w.getList());
             nde = w.getCurrentTree();
             //doc = null;
             
  
             int pathNode = 0;  //LinkedListNode<XmlNode> pathNode = path.First;
 
             int max = 0;
             for (XTreeNode item : lst)
             {
                 ELinkedList<Integer> ags = item.getGroupList();
                 for (int grp : ags)
                 {
                     if (grp > max)
                         max = grp;
                 }
             }
             ELinkedList<Node>[] allLists = (ELinkedList<Node>[]) Array.newInstance(ELinkedList.class, max);
             for (int i = 0; i < max; i++)
                 allLists[i] = new ELinkedList<Node>();
             ELinkedList<Node[]> regexCaptures = new ELinkedList<Node[]>();
             int n = 0;//LinkedListNode<XTreeNode> n = lst.First;
             boolean skipAttribVal = false;
             while (n < lst.size())
             {
                 String str = lst.get(n).getToken();
                 char c = str.charAt(0);
                 
                 switch (c)
                 {
                     case '/':  //these tokens may or may not have a node associated with it
                         if (n < lst.size() && str.length() == 1)
                         {
                             n++;
                             continue;
                         }
                         break;
                     case '\\':
                     case '<':
                     case '>':
                         if (n < lst.size())
                         {
                             n++;
                             continue;
                         }
                         break;
                     
                     case '@':   //these tokens do not have a node associated with it
                     case '\"':
                         n++;
                         continue;
                     case '=':  //this token has no node associated with it, nor does the token following it ( (@attrib)=val - only @attrib is grabbed))
                         n++;
                         skipAttribVal = true;
                         continue;
                     
                 }
                 
                 ELinkedList<Integer> allGroups = lst.get(n).getGroupList();
                 //bool addedCaptures = false;
                 Node[][] captures = null;  //regular expression captures
                 //bool isRegex = false;
                 if (skipAttribVal == false)
                 {
                     if (lst.get(n).isRegex() && path.get(pathNode).getNodeType() == Node.TEXT_NODE)
                         captures = w.getRegexParts(path.get(pathNode).getTextContent(), lst.get(n).getToken().substring(1), lst.get(n).getRegexMatchNo());
                     for (int item : allGroups)
                     {
                         if(captures != null)
                         {
                             for (int i = 0; i < 3; i++)
                                 allLists[item - 1].addLast(captures[0][i]);
                         }
                         else
                             allLists[item - 1].addLast(/*(XmlElement)*/ path.get(pathNode));
                     }
                     pathNode++;
                 }
                 else  //you are looking at an attribute value
                 {
                     String attribVal = "";
                     Attr refAttrib = null;
                     if (path.get(pathNode) == null)  //the last attrib value in a list
                         refAttrib =(Attr) path.getLast();
                     else if (path.get(pathNode - 1) != null)  //this may not be a necessary check
                         refAttrib = (Attr) path.get(pathNode - 1);
                     if (refAttrib != null)  //this may not be a necessary check
                         attribVal = refAttrib.getValue();
                     if (lst.get(n).isRegex())
                     {
                         String regexToken = lst.get(n).getToken();
                         regexToken = regexToken.substring(1);
                         captures = w.getRegexParts(attribVal, regexToken, lst.get(n).getRegexMatchNo());
                     }
                     for(int item : allGroups)
                     {
                         if (!allLists[item - 1].contains(refAttrib))  //if the group does not currently contain the attribute value's node
                         {
                             Attr xa = nde.getOwnerDocument().createAttribute("__String__Value__Only__Attribute__");
                             xa.setValue(attribVal);// = attribVal;
 
                             if (captures != null)
                             {
                                 for (int i = 0; i < 3; i++)
                                     allLists[item - 1].addLast(captures[0][i]);
                             }
                             else
                                 allLists[item - 1].addLast(xa);
                         }
                     }
                     skipAttribVal = false;
                 }
                 if (captures != null)
                 {
                     for (int i = 1; i < captures.length; i++)
                         regexCaptures.addLast(captures[i]);
                 }
                 n++;
             }
             match = path.toArray(new Node[0]);   //populate match[0]
             //match = new XmlElement[xn.Length];
             //for (int i = 0; i < xn.Length; i++)
                 //match[i] = (XmlElement) xn[i];
             max += regexCaptures.size();
             groups = new Node[max][];
             int j = 0;
             for (ELinkedList<Node> list : allLists)
             {
                 groups[j] = list.toArray(new Node[0]);
                 j++;
             }
             for (Node[] item : regexCaptures)
             {
                 groups[j] = item;
                 j++;
             }
             depth = calcDepth(match[0]);
         }
 
         public Node[] get()
         {
             return match;
         }
 
         public Node[] get(int index)
         {
             return groups[index - 1];
         }
 
         public int getGroupCount()
         {
             return groups.length;
         }
 
         private int calcDepth(Node nde)
         {
             Document od = nde.getOwnerDocument();
             int d = 0;
             Node copy = nde;
             while (nde != od.getDocumentElement())
             {
                 nde = nde.getParentNode();
                 d++;
                 if (nde == null)
                     break;
             }
             return d;
         }
 
         public int getDepth()
         {
             return depth;
         }
     }
 //}
 
 
 
 public class XPatternTest 
 {
 	public static void main(String args[])
 	{
 		Document d = XmlUtils.loadXML("src\\XPattern1.xml");
 		XmlUtils.saveXML(d, "src\\XPattern1.xml");
 		d = XmlUtils.loadXML("src\\XPattern1.xml");
 		XPattern xp1 = new XPattern(d);
 		//FIND
 		//directional find
 		NodeGroup[] ng1 = xp1.findAll("method\\param");
 		expect(2, ng1);
 		//ng1 = xp1.findAll("method/class");
 		//expect(2, ng1);
 		//attribute find
 		ng1 = xp1.findAll("method@rtnType");
 		expect(1, ng1);
 		ng1 = xp1.findAll("class@name=Goodie");
 		expect(0, ng1);
 		//text and CDATA find
 		ng1 = xp1.findAll("desc\\\"");
 		expect(1, ng1);
 		ng1 = xp1.findAll("class\\sample\\CDATA");
 		expect(1, ng1);
 		//regex text find
 		ng1 = xp1.findAll("\"/no");
 		expect(1, ng1);
 		//negated find
 		//ng1 = xp1.findAll("method\\!param");
 		//expect(1, ng1);
 		ng1 = xp1.findAll("class\\!method");
 		expect(3, ng1);
 		//perhaps find attribute
 		ng1 = xp1.findAll("method@name@rtnType?");
 		expect(2, ng1);
 		//parameterized find
 		
 		
 		//multiple node find
 		ng1 = xp1.findAll("method\\param*");  //lookahead condition
 		expect(2, ng1);
 		
 		ng1 = xp1.findAll("method\\param+@datatype=String");
 		expect(1, ng1);
 		
 		//without >
 		ng1 = xp1.findAll("param>-desc");
 		expect(2, ng1);
 		//without <
 		ng1 = xp1.findAll("param<-param");
 		expect(1, ng1);
 		
 		//without \
 		ng1 = xp1.findAll("method\\-param");
 		expect(1, ng1);
 		ng1 = xp1.findAll("method\\-desc");
 		expect(2, ng1);
 		
 		//attribute finding
 		ng1 = xp1.findAll("@name");
 		expect(5, ng1);
 		
 		//REPLACE
 		//directional replace
 		ng1 = xp1.replaceAll("vars", "fields");
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		
 		NodeGroup ngx = xp1.replace("fields", "fields\\field@name=a@datatype=String>field@name=b@datatype=string");
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		//attribute replace
 		ng1 = xp1.replaceAll("(field)@name=a\\-desc", "&1\\desc\\\"This variable sux");
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		ng1 = xp1.replaceAll("field\\(desc)\\\"/(variable)", "^1desc\\\"$`2>ref\\\"$2/>\"$'2");
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		//text and CDATA replace
 		
 		//$n - node only
 		
 		//%n - children only
 		
 		
 		
 		
 		ng1 = xp1.replaceAll("(field)@name=(*)@datatype=(*)", "var@name=$2@datatype=$3%1");
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		
 		ng1 = xp1.replaceAll("(fields)", "vars%1");
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		//&n - node & children
 		
 		//^n - move insertion point
 		xp1.replaceAll("class\\vars\\(var)(@**)", "^1variable@$2%1");
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		//parameterized replace
 		String[] replacers = {"aa", "bbb"};
 		xp1.replaceAll("class\\(desc)\\\"(*)", "^1desc@#0=#1$2", replacers);  //look at how the nodeGroup is made for this one
 		System.out.println(XmlUtils.outerXml(d.getDocumentElement()));
 		
 	}
 	
 	public static void expect(int ex, NodeGroup[] ng)
 	{
 		if(ex != ng.length)
 			System.err.println("Expected " + ex + ", actual " + ng.length);
 		else
 			System.out.println("OK");
 	}
 }
