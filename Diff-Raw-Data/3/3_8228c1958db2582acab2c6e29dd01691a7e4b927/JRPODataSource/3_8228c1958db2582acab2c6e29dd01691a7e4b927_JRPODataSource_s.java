 /**
  * 
  */
 package org.opensixen.report;
 
 import java.util.List;
 
 import org.compiere.model.PO;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRField;
 import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;
 
 /**
  * @author harlock
  *
  */
 public class JRPODataSource extends JRAbstractBeanDataSource{
 
	private int index = 0;
 	
 	private PO[] po;
 	
 	
 	/**
 	 * Constructor
 	 */
 	public <T extends PO>JRPODataSource(T[] data ) {
 		super(false);
 		this.po = data;
 	}
 	
 	public <T extends PO> JRPODataSource(List<T> data)	{
 		super(false);
 		po = data.toArray(new PO[data.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jasperreports.engine.JRRewindableDataSource#moveFirst()
 	 */
 	@Override
 	public void moveFirst() throws JRException {
 		index = 0;
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
 	 */
 	@Override
 	public Object getFieldValue(JRField field) throws JRException {
 		return po[index].get_Value(field.getName());
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jasperreports.engine.JRDataSource#next()
 	 */
 	@Override
 	public boolean next() throws JRException {
 		if ((index + 1) < po.length)	{
 			index++;
 			return true;
 		}
 		return false;
 	}
 }
