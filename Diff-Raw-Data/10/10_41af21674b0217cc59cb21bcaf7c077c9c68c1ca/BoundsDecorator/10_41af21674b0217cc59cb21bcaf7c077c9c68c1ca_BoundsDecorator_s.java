 package org.dawnsci.common.widgets.decorator;
 
 import java.text.NumberFormat;
 
 import javax.swing.event.EventListenerList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * A regexp decorator which will only accept numbers and 
  * will color invalid bounds red.
  * 
  * @author fcp94556
  *
  */
 public class BoundsDecorator extends RegexDecorator {
 	
 	private enum BoundsType {
 		MINIMUM, MAXIMUM; // Other types like non-inclusive bound possible
 	}
 
     private Object       maximum;
     private Object       minimum;
     private NumberFormat numberFormat;
 	private boolean      isError=false;
 
 	public BoundsDecorator(Text text, String stringPattern, NumberFormat numFormat) {
 		super(text, stringPattern);
 		numberFormat = numFormat;
 	}
 	
 	@Override
 	protected boolean check(String totalString, String delta) {
 		Number val = null;
 		try {
 			val = parseValue(totalString);
 		} catch (Throwable ne) {
 			return "".equals(delta) || delta==null || "-".equals(totalString); // Will not allow current value to proceed.
 		}
 		if (val==null) return false;
 		
 		checkBounds(val, true); // Colors red not unacceptable value.
 		
 		return true;
 	}
 
 	private Number parseValue(String totalString) {
 		Number val = null;
 		if ("∞".equals(totalString)) {
 			val = Double.POSITIVE_INFINITY;
 		} else if ("-∞".equals(totalString)) {
 			val = Double.NEGATIVE_INFINITY;
 		} else {
		    val = Double.parseDouble(totalString);
 		}
 		return val;
 	}
 
 	public Number getValue() {
 		return parseValue(text.getText());
 	}
 	
 	public void setValue(Number value) {
 		if (value.doubleValue() == Double.POSITIVE_INFINITY) {
 		    text.setText("∞");	
 		} else if (value.doubleValue() == Double.NEGATIVE_INFINITY) {
 		    text.setText("-∞");	
 		} else {
 		    text.setText(numberFormat.format(value));
 		}
 	}
 
 	private void checkBounds(final Number value, boolean fireListeners) {
 		checkBounds(value, getMinimum(), getMaximum(), fireListeners);
 	}
 	
 	private void checkBounds(final Number value, Number min, Number max, boolean fireListeners) {
 
 		try {
 			if (Double.isInfinite(value.doubleValue())) {
 				setError(false, createToolTipTextFromBounds(value, getMinimum(), getMaximum()));
 				return;
 			}
 			
 			if (!checkValue(value, min, BoundsType.MINIMUM)) {
 				setError(true, value+" is less than the minimum value of "+getMinimum());
 				return;
 			}
 			if (!checkValue(value, max, BoundsType.MAXIMUM)) {
 				setError(true, value+" is greater than the maximum value of "+getMaximum());
 				return;
 			}
 			setError(false, createToolTipTextFromBounds(value, getMinimum(), getMaximum()));
 		} finally {
 			// We let this current value take then fire a check later.
 			fireValueChangedListeners(new ValueChangeEvent(BoundsDecorator.this, value));
 		}
 	}
 
 	private boolean checkValue(Number value, Number bound, BoundsType type) {
 		if (value == null) return false;
 		if (bound == null) return true;
 		
 		switch (type){
 		case MINIMUM:
 			return value.doubleValue()>=bound.doubleValue();
 		case MAXIMUM:
 			return value.doubleValue()<=bound.doubleValue();
 		}
 		throw new RuntimeException("Invalid value!");
 	}
 
 	private EventListenerList listeners;
 	
 	protected void fireValueChangedListeners(final ValueChangeEvent evt) {
 		if (listeners == null) return;
 		
 		final IValueChangeListener[] ls = listeners.getListeners(IValueChangeListener.class);
 		if (ls==null || ls.length<1) return;
 		
 		for (IValueChangeListener l : ls) {
 			l.valueValidating(evt);
 		}
 
 	}
 	public void addValueChangeListener(IValueChangeListener l) {
 		if (listeners == null) listeners = new EventListenerList();
 		listeners.add(IValueChangeListener.class, l);
 	}
 	public void removeValueChangeListener(IValueChangeListener l) {
 		if (listeners == null) return;
 		listeners.remove(IValueChangeListener.class, l);
 	}
 
 	private String createToolTipTextFromBounds(Number value, Number min, Number max) {
 
 		if (min==null && max==null) return "Please enter a number.";
 		
 		final StringBuilder buf = new StringBuilder();
 
 		if (min!=null) {
 			if (Double.isInfinite(min.doubleValue())) {
 				buf.append("-∞");
 			} else {
 				buf.append(numberFormat.format(min));
 			}
 			buf.append(" <= ");
 		}
 		
 		buf.append(numberFormat.format(value));
 
 		if (max!=null) {
 			
 			buf.append(" <= ");
 
 			if (Double.isInfinite(max.doubleValue())) {
 				buf.append("∞");
 			} else {
 				buf.append(numberFormat.format(max));
 			}
 		}
         return buf.toString();
 	}
 
 	private void setError(boolean isError, String toolTip) {
 		this.isError = isError;
 		text.setToolTipText(toolTip);
 		if (isError) {
 			text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED)); 
 		} else {
 			text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK)); 
 		}
 	}
 
 
 	public Number getMaximum() {
 		if (maximum == null) return null;
 		return maximum instanceof Number ? (Number)maximum : ((BoundsDecorator)maximum).getValue();
 	}
 
 	public void setMaximum(Object maximum) {
 		this.maximum = maximum;
 		registerBoundsChanger(maximum, BoundsType.MAXIMUM);
 		if (text.getText()==null || "".equals(text.getText())) return;
 		checkBounds(getValue(), false);
 	}
 
 	private void registerBoundsChanger(Object object, final BoundsType type) {
 		if (object == this) return;
 		if (object instanceof BoundsDecorator) {
 		    ((BoundsDecorator)object).addValueChangeListener(new IValueChangeListener() {
 				@Override
 				public void valueValidating(ValueChangeEvent evt) {
 					if (evt.getSource()==this) return;
 					
 					Number value = getValue();
 					boolean isOk = checkValue(value, evt.getValue(), type);
 					if (!isOk) {
 						if (type==BoundsType.MINIMUM) {
 						    setError(true, value+" is less than the minimum value of "+evt.getValue());
 						} else if (type == BoundsType.MAXIMUM) {
 							setError(true, value+" is greater than the maximum value of "+evt.getValue());
 						}
 						return;
 					} else {
 						if (type==BoundsType.MINIMUM) {
 							setError(false, createToolTipTextFromBounds(value, evt.getValue(), getMaximum())); 
 						} else if (type == BoundsType.MAXIMUM) {
 							setError(false, createToolTipTextFromBounds(value, getMinimum(), evt.getValue())); 
 						}
 					}
 				}
 		    });
 		}
 	}
 
 	public Number getMinimum() {
 		if (minimum == null) return null;
 		return minimum instanceof Number ? (Number)minimum : ((BoundsDecorator)minimum).getValue();
 	}
 
 	public void setMinimum(Object minimum) {
 		this.minimum = minimum;
 		registerBoundsChanger(minimum, BoundsType.MINIMUM);
 		if (text.getText()==null || "".equals(text.getText())) return;
 		checkBounds(getValue(), false);
 	}
 
 	public NumberFormat getNumberFormat() {
 		return numberFormat;
 	}
 
 	public void setNumberFormat(NumberFormat numberFormat) {
 		this.numberFormat = numberFormat;
 	}
 
 	public boolean isError() {
 		return isError;
 	}
 
 }
