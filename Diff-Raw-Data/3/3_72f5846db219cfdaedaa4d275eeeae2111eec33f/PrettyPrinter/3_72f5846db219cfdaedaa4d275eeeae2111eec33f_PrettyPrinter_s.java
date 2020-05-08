 package org.cipango.diameter.util;
 
 import org.cipango.diameter.AVP;
 import org.cipango.diameter.AVPList;
 import org.cipango.diameter.api.DiameterServletAnswer;
 import org.cipango.diameter.base.Accounting;
 import org.cipango.diameter.ims.Zh;
 import org.cipango.diameter.node.DiameterMessage;
 import org.cipango.diameter.node.DiameterRequest;
 import org.cipango.diameter.node.Node;
 import org.eclipse.jetty.util.StringUtil;
 
 public class PrettyPrinter implements DiameterVisitor
 {
 	private int _index;
 	private StringBuilder _buffer;
 	
 	public void visit(DiameterMessage message) 
 	{
 		_index = 0;
 		_buffer = new StringBuilder();
 		_buffer.append("[appId=").append(message.getApplicationId());
 		_buffer.append(",e2eId=").append(message.getEndToEndId());
 		_buffer.append(",hopId=").append(message.getHopByHopId()).append("] ");
 		_buffer.append(message.getCommand());
 		
 		if (message instanceof DiameterServletAnswer)
 			_buffer.append(" / ").append(((DiameterServletAnswer) message).getResultCode());
  
 		_buffer.append(StringUtil.__LINE_SEPARATOR);
 	}
 
 	public void visit(AVP<?> avp)
 	{
 		if (!(avp.getValue() instanceof AVPList))
 		{
 			for (int i = 0; i < _index; i++)
 				_buffer.append("    ");
 			_buffer.append(avp.getType()).append(" = ");
 			
 			if (avp.getValue() instanceof byte[])
 			{
 				byte[] tab = (byte[]) avp.getValue();
				if (tab[0] == '<' && tab[1] == '?' && tab[2] == 'x' && tab[3] == 'm' && tab[4] == 'l')
 					_buffer.append(new String(tab));
 				else
 					_buffer.append(tab);
 			}
 			else
 				_buffer.append(avp.getValue());
 			_buffer.append(StringUtil.__LINE_SEPARATOR);
 		}
 	}
 	
 	public void visitEnter(AVP<AVPList> avp)
 	{
 		_buffer.append(avp.getType() + " = ");
 		_buffer.append(StringUtil.__LINE_SEPARATOR);
 		_index++;
 	}
 	
 	public void visitLeave(AVP<AVPList> avp)
 	{
 		_index--;
 	}
 	
 	public String toString()
 	{
 		return _buffer.toString();
 	}
 	
 	public static void main(String[] args) 
 	{
 		DiameterMessage message = new DiameterRequest(new Node(), Accounting.ACR, Accounting.ACCOUNTING_ORDINAL, "foo");
 		message.getAVPs().add(Zh.ZH_APPLICATION_ID.getAVP());
 
 		PrettyPrinter pp = new PrettyPrinter();
 		message.accept(pp);
 		System.out.println(pp);
 	}
 }
