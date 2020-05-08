/**
 * 
 */
 package de.ptb.epics.eve.viewer.views.engineview;
 
 import org.eclipse.core.databinding.conversion.IConverter;
 
 /**
 * @author mmichals
 *
  */
 public class RepeatCountModelToTargetConverter implements IConverter {
 
	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.conversion.IConverter#getFromType()
 	 */
 	@Override
 	public Object getFromType() {
 		return Integer.class;
 	}
 
	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.conversion.IConverter#getToType()
 	 */
 	@Override
 	public Object getToType() {
 		return String.class;
 	}
 
	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
 	 */
 	@Override
 	public Object convert(Object fromObject) {
		return ((Long)fromObject).toString();
 	}
 
 }
