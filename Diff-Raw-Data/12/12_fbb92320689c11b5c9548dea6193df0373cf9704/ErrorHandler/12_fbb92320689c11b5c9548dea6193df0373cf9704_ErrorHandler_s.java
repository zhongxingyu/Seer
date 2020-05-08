 package pt.com.broker.core;
 
 import java.io.PrintWriter;
 
 import org.apache.mina.util.ExceptionMonitor;
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.text.StringBuilderWriter;
 import org.caudexorigo.text.StringUtils;
 import org.jibx.runtime.JiBXException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.sun.management.GcInfo;

 import pt.com.broker.xml.EndPointReference;
 import pt.com.broker.xml.SoapEnvelope;
 import pt.com.broker.xml.SoapFault;
 import pt.com.broker.xml.SoapHeader;
 import pt.com.gcs.conf.GcsInfo;
 
 public class ErrorHandler extends ExceptionMonitor
 {
 	private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
 
 	public void exceptionCaught(Throwable cause)
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(cause);
 		if (log.isWarnEnabled())
 		{
 			log.error("Unexpected exception.", rootCause);
 		}
 		exitIfCritical(rootCause);
 	}
 
 	public static void checkAbort(Throwable t)
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(t);
 		exitIfCritical(rootCause);
 	}
 
 	public static WTF buildSoapFault(Throwable ex)
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(ex);
 		exitIfCritical(rootCause);
 
 		String ereason = "soap:Receiver";
 		if (rootCause instanceof JiBXException)
 		{
 			ereason = "soap:Sender";
 		}
 		else if (rootCause instanceof IllegalArgumentException)
 		{
 			ereason = "soap:Sender";
 		}
 
 		return _buildSoapFault(ereason, rootCause);
 	}
 
 	public static WTF buildSoapFault(String faultCode, Throwable ex)
 	{
 		if (StringUtils.isBlank(faultCode))
 		{
 			return buildSoapFault(ex);
 		}
 
 		Throwable rootCause = ErrorAnalyser.findRootCause(ex);
 		exitIfCritical(rootCause);
 		return _buildSoapFault(faultCode, rootCause);
 	}
 
 	private static WTF _buildSoapFault(String faultCode, Throwable ex)
 	{
 		StringBuilderWriter sw = new StringBuilderWriter();
 		PrintWriter pw = new PrintWriter(sw);
 		ex.printStackTrace(pw);
 		String reason = ex.getMessage();
 		String detail = sw.toString();
 
 		SoapEnvelope faultMessage = new SoapEnvelope();
 		SoapFault sfault = new SoapFault();
 		sfault.faultCode.value = faultCode;
 		sfault.faultReason.text = reason;
 		sfault.detail = detail;
 		faultMessage.body.fault = sfault;
 		
 
 		SoapHeader soap_header = new SoapHeader();
 		EndPointReference epr = new EndPointReference();
 		epr.address = GcsInfo.getAgentName();
 		soap_header.wsaFrom = epr;
 		faultMessage.header = soap_header;
 		
 
 		WTF wtf = new WTF();
 		wtf.Cause = ex;
 		wtf.Message = faultMessage;
 		
 		
 
 		return wtf;
 	}
 
 	public static class WTF
 	{
 		public SoapEnvelope Message;
 
 		public Throwable Cause;
 	}
 	
 	private static void exitIfUlimit(Throwable t)
 	{
 		String ul_error = "Too many open files".toLowerCase();
 		if (t.getMessage()!=null)
 		{
 			String emsg = t.getMessage().toLowerCase();
 			if (emsg.contains(ul_error))
 			{
 				Shutdown.now();
 			}
 		}
 	}
 	
 	private static void exitIfCritical(Throwable cause)
 	{
 		ErrorAnalyser.exitIfOOM(cause);
 		exitIfUlimit(cause);
 	}
 
 }
