 package de.ptb.epics.eve.viewer.views.engineview;
 
 import org.eclipse.core.databinding.conversion.IConverter;
 
 /**
 * @author Marcus Michalsky
 * @since 1.20
  */
 public class RepeatCountModelToTargetConverter implements IConverter {
 
	/**
	 * {@inheritDoc}
 	 */
 	@Override
 	public Object getFromType() {
 		return Integer.class;
 	}
 
	/**
	 * {@inheritDoc}
 	 */
 	@Override
 	public Object getToType() {
 		return String.class;
 	}
 
	/**
	 * {@inheritDoc}
 	 */
 	@Override
 	public Object convert(Object fromObject) {
		return ((Integer)fromObject).toString();
 	}
 
 }
