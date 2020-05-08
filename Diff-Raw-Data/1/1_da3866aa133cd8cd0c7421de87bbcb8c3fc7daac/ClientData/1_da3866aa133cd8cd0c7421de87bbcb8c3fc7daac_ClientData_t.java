 package com.jdevelopstation.l2ce.version.node.data;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Iterator;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentFactory;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 import com.jdevelopstation.l2ce.version.ClientVersion;
 import com.jdevelopstation.l2ce.version.node.ClientNodeContainer;
 import com.jdevelopstation.l2ce.version.node.data.impl.ClientDataBlockNodeImpl;
 import com.jdevelopstation.l2ce.version.node.data.impl.ClientDataForNodeImpl;
 import com.jdevelopstation.l2ce.version.node.data.impl.ClientDataNodeImpl;
 import com.jdevelopstation.l2ce.version.node.file.ClientFile;
 import com.jdevelopstation.l2ce.version.node.file.ClientFileNode;
 import com.jdevelopstation.l2ce.version.node.file.impl.ClientFileForNodeImpl;
 import com.jdevelopstation.l2ce.version.node.file.impl.ClientFileNodeImpl;
 
 /**
  * @author VISTALL
  * @date 8:50/18.05.2011
  */
 public class ClientData extends ClientNodeContainer<ClientDataNode>
 {
 	private String _file;
 
 	public ClientData()
 	{
 		//
 	}
 
 	public ClientData(String pattern)
 	{
 		_file = pattern;
 	}
 
 	public void fromXML(ClientVersion version, String in)
 	{
 		File f = new File(in);
 		if(!f.exists())
 			return;
 
 		SAXReader reader = new SAXReader();
 		reader.setValidation(false);
 
 		try
 		{
 			Document doc = reader.read(f);
 			Element rootElement = doc.getRootElement();
 
 			_file = rootElement.attributeValue("name");
 
 			ClientFile clientFile = null;
 			for(ClientFile file : version.getClientFiles())
 				if(file.getPattern().matcher(_file).find())
 				{
 					clientFile = file;
 					break;
 				}
 
 			if(clientFile == null)
 				return;
 
 			read(this, clientFile, rootElement);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void read(ClientNodeContainer<ClientDataNode> container, ClientNodeContainer<ClientFileNode> container2, Element xmlElement)
 	{
 		initData(container, container2);
 
 		for(int i = 0; i < container.size(); i++)
 		{
 			ClientDataNode dataNode = container.get(i);
 			ClientFileNode fileNode = container2.get(i);
 			if(dataNode.isHidden())
 				continue;
 
 			if(dataNode instanceof ClientDataNodeImpl)
 			{
 				if(fileNode.getValue() != null)
 					dataNode.setValue(fileNode.getValue());
 				else
 					dataNode.setValue(xmlElement.element(dataNode.getName()).getText());
 			}
 			else if(dataNode instanceof ClientDataForNodeImpl)
 			{
 				long index = 0;
 
 				ClientDataForNodeImpl forDataNode = (ClientDataForNodeImpl) dataNode;
 				for(Iterator<Element> iterator = xmlElement.elementIterator(forDataNode.getForName()); iterator.hasNext(); index++)
 				{
 					Element e = iterator.next();
 
 					ClientDataBlockNodeImpl blockNode = new ClientDataBlockNodeImpl((ClientFileForNodeImpl) fileNode, index);
 
 					read(blockNode, (ClientFileForNodeImpl) fileNode, e);
 
 					forDataNode.add(blockNode);
 				}
 
 				if(forDataNode.getForNode().getFixed() > 0)
 				{
 					//
 				}
 				else
 				{
 					ClientDataNode node = container.getNodeByName(forDataNode.getForName());
 
 					Long val = (Long) node.getValue();
 					if(val == Long.MIN_VALUE)
 						node.setValue(index);
 				}
 			}
 		}
 	}
 
 	private void initData(ClientNodeContainer<ClientDataNode> container, ClientNodeContainer<ClientFileNode> container2)
 	{
 		for(ClientFileNode node : container2)
 		{
 			if(node instanceof ClientFileNodeImpl)
 			{
 				ClientDataNodeImpl data = new ClientDataNodeImpl(node);
 
 				container.add(data);
 			}
 			else if(node instanceof ClientFileForNodeImpl)
 			{
 				ClientFileForNodeImpl fileForNode = (ClientFileForNodeImpl) node;
 
 				// создаем цыкл с этим названия НОДе
 				ClientDataForNodeImpl forNode = new ClientDataForNodeImpl(fileForNode);
 
 				if(fileForNode.getFixed() > 0)
 				{
 
 				}
 				else
 				{
 					// ищем размер
 					ClientDataNode forIndex = container.getNodeByName(fileForNode.getForName());
 					forNode.setSizeNode(forIndex);
 
 					// ставим дефаулт значения
 					forIndex.setValue(Long.MIN_VALUE);
 					forIndex.setHidden(true);
 				}
 				container.add(forNode);
 			}
 		}
 	}
 
 	public void toDat(File out)
 	{
 		ByteBuffer byteBuffer = ByteBuffer.allocate(0xFFFFFF);
 		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
 
 		for(ClientDataNode node : this)
 			write(node, byteBuffer);
 
 		int position = byteBuffer.position();
 		byteBuffer.position(0);
 
 		byte[] data = new byte[position];
 		byteBuffer.get(data);
 
 		try
 		{
 			FileOutputStream steam = new FileOutputStream(out);
 			steam.write(data);
 			steam.close();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
	@SuppressWarnings("unchecked")
 	private void write(ClientDataNode node, ByteBuffer buffer)
 	{
 		if(node instanceof ClientDataNodeImpl)
 		{
 			ClientDataNodeImpl data = (ClientDataNodeImpl) node;
 
 			data.write(buffer);
 		}
 		else if(node instanceof ClientNodeContainer)
 		{
 			ClientNodeContainer<ClientDataNode> container = (ClientNodeContainer) node;
 			for(ClientDataNode n : container)
 				write(n, buffer);
 		}
 	}
 
 	public void toXML(String out)
 	{
 		File f = new File(out);
 		if(f.exists())
 			f.delete();
 
 		try
 		{
 			Document doc = DocumentFactory.getInstance().createDocument();
 
 			FileOutputStream fos = new FileOutputStream(f);
 			OutputFormat format = OutputFormat.createPrettyPrint();
 			format.setIndent("\t");
 
 			Element rootElement = doc.addElement("out");
 			rootElement.addAttribute("name", _file);
 
 			for(ClientDataNode node : getNodes())
 				node.toXML(rootElement);
 
 			XMLWriter writer = new XMLWriter(fos, format);
 			writer.write(doc);
 			writer.close();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 }
