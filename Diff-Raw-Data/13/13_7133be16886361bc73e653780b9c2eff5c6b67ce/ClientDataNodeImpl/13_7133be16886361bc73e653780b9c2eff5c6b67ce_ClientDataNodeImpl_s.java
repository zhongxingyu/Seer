 package com.jdevelopstation.l2ce.version.node.data.impl;
 
 import java.nio.ByteBuffer;
 
 import org.dom4j.Element;
 import com.jdevelopstation.l2ce.version.node.data.ClientDataNode;
 import com.jdevelopstation.l2ce.version.node.file.ClientFileNode;
 
 /**
  * @author VISTALL
  * @date 8:42/18.05.2011
  */
 public class ClientDataNodeImpl implements ClientDataNode
 {
 	private Object _value;
 
 	private boolean _hidden;
 
 	private final ClientFileNode _fileNode;
 
 	public ClientDataNodeImpl(ClientFileNode node)
 	{
 		_fileNode = node;
 	}
 
 	@Override
 	public String getName()
 	{
 		return _fileNode.getName();
 	}
 
 	public Object getValue()
 	{
 		return _value;
 	}
 
 	@Override
 	public void toXML(Element element)
 	{
 		if(isHidden())
 			return;
 
  		Element e = element.addElement(getName());
 		e.setText(String.valueOf(getValue()));
 	}
 
 	public void write(ByteBuffer buffer)
 	{
 		_fileNode.getReaderType().write(_value, buffer);
 	}
 
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[name: " + getName() + "; value: " + _value + "]";
 	}
 
 	@Override
 	public boolean isHidden()
 	{
 		return _hidden;
 	}
 
 	@Override
 	public void setHidden(boolean hidden)
 	{
 		_hidden = hidden;
 	}
 
 	@Override
 	public void setValue(Object value)
 	{
 		_value = value;
 	}
 }
