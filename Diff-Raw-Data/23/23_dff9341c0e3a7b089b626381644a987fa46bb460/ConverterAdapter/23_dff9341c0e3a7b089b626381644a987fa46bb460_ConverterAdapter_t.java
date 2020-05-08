 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.edp.adapters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.wazaabi.engine.edp.EDP;
 import org.eclipse.wazaabi.engine.edp.EDPSingletons;
 import org.eclipse.wazaabi.engine.edp.converters.BundledConverter;
 import org.eclipse.wazaabi.engine.edp.exceptions.OperationAborted;
 import org.eclipse.wazaabi.mm.edp.EventDispatcher;
 import org.eclipse.wazaabi.mm.edp.events.Event;
 import org.eclipse.wazaabi.mm.edp.handlers.Converter;
 import org.eclipse.wazaabi.mm.edp.handlers.EDPHandlersPackage;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 import org.eclipse.wazaabi.mm.edp.handlers.Executable;
 
 public class ConverterAdapter extends ActionAdapterImpl {
 
 	private static final MethodSignature[] METHOD_SIGNATURES = new MethodSignature[] { new MethodSignature(
 			"convert", new String[] { "input" }, new Class[] { Object.class },
 			Object.class) };
 
 	private BundledConverter bundledConverter = null;
 	private String bundledConverterId = null;
 
 	public void setTarget(Notifier newTarget) {
 		if (newTarget != null) {
 			// We allow the converterAdapter to resolve both the OSGi DS
 			// converter
 			// referenced by its ID and the deferred converter.
 			// At run time priority goes to the OSGi DS converter.
 			String converterId = ((Converter) newTarget).getId();
			if (converterId != null && converterId.length() != 0) {
 				if (EDPSingletons.getComposedBundledConverterFactory() != null)
 					bundledConverter = EDPSingletons
 							.getComposedBundledConverterFactory()
 							.createBundledConverter(this, converterId);
 
 				if (bundledConverter == null)
 					throw new RuntimeException("no validator found"); //$NON-NLS-1$
 				// TODO : we need to log that
 			}
 		} else
 			detachBundledConverter();
 
 		super.setTarget(newTarget);
 	}
 
 	@Override
 	public void trigger(EventDispatcher eventDispatcher,
 			EventHandler eventHandler, Event event) throws OperationAborted {
 		try {
 			Object sourceValue = eventDispatcher.get(EDP.VALUE_SOURCE_KEY);
 			Object convert = null;
 			if (sourceValue instanceof List) {
 				if (((List<?>) sourceValue).size() == 1) {
 					convert = this.convert(((List<?>) sourceValue).get(0));
 					List<Object> result = new ArrayList<Object>();
 					result.add(convert);
 					eventDispatcher.set(EDP.CONVERTER_RESULT_KEY, result);
 				} else {
 					convert = this.convert(sourceValue);
 					eventDispatcher.set(EDP.CONVERTER_RESULT_KEY, convert);
 				}
 			}
 
 		} catch (Exception e) {
 			throw new OperationAborted(this);
 		}
 
 	}
 
 	public Object convert(Object input) {
 		Object returnedValue = null;
 		if (bundledConverter != null)
 			returnedValue = bundledConverter.convert(input);
 		else if (getMethodDescriptor(0) != null)
 			returnedValue = getCodeDescriptor().invokeMethod(
 					getMethodDescriptor(0), new Object[] { input });
 
 		return returnedValue;
 	}
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		switch (notification.getFeatureID(Executable.class)) {
 		case EDPHandlersPackage.EXECUTABLE__ID:
 			switch (notification.getEventType()) {
 			case Notification.SET:
 				attachBundledConverter(notification.getNewStringValue());
 				break;
 			}
 		}
 		super.notifyChanged(notification);
 	}
 
 	@Override
 	public boolean isAdapterForType(Object type) {
 		return type instanceof Converter;
 	}
 
 	public MethodSignature[] getMethodSignatures() {
 		return METHOD_SIGNATURES;
 	}
 
 	protected void attachBundledConverter(String bundleConverterId) {
 		// we don't attach the same BundledConverter
 		if (bundleConverterId != null
 				&& bundleConverterId.equals(this.bundledConverterId)
 				&& bundledConverter != null && !bundledConverter.isDisposed())
 			return;
 		detachBundledConverter();
		if (bundleConverterId != null && bundleConverterId.length() != 0) {
 			bundledConverter = EDPSingletons
 					.getComposedBundledConverterFactory()
 					.createBundledConverter(this, bundleConverterId);
 			if (bundledConverter != null)
 				this.bundledConverterId = bundleConverterId;
 			else
 				this.bundledConverterId = null;
 		}
 
 	}
 
 	protected void detachBundledConverter() {
 		if (bundledConverter != null && !bundledConverter.isDisposed()) {
 			bundledConverter.dispose();
 			bundledConverter = null;
 			this.bundledConverterId = null;
 		}
 	}
 
 }
