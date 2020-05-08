 /*
  *	Copyright 2006-2012, Rathravane LLC
  *
  *	Licensed under the Apache License, Version 2.0 (the "License");
  *	you may not use this file except in compliance with the License.
  *	You may obtain a copy of the License at
  *	
  *	http://www.apache.org/licenses/LICENSE-2.0
  *	
  *	Unless required by applicable law or agreed to in writing, software
  *	distributed under the License is distributed on an "AS IS" BASIS,
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *	See the License for the specific language governing permissions and
  *	limitations under the License.
  */
 package com.rathravane.drumlin.app.htmlForms;
 
 import java.util.LinkedList;
 
 import com.rathravane.drumlin.service.framework.context.handlingContext;
 
 public class webHtmlFormFieldInfo
 {
 	public webHtmlFormFieldInfo ( String fn )
 	{
 		fFieldName = fn;
 		fSteps = new LinkedList<webHtmlFormValidationStep> ();
 	}
 
 	public void validate ( String fieldName, handlingContext context, webHtmlFormPostWrapper form, webHtmlFormValidationError err )
 	{
 		for ( webHtmlFormValidationStep step : fSteps )
 		{
 			step.validate ( context, form, this, err );
 		}
 	}
 
 	public webHtmlFormFieldInfo validateWith ( webHtmlFormValidationStep step )
 	{
 		fSteps.add ( step );
 		return this;
 	}
 
 	public webHtmlFormFieldInfo required ( final String errMsg )
 	{
 		return validateWith ( new webHtmlFormValidationStep ()
 		{
 			@Override
 			public void validate ( handlingContext context, webHtmlFormPostWrapper form, webHtmlFormFieldInfo field, webHtmlFormValidationError err )
 			{
 				if ( !form.hasParameter ( fFieldName ) )
 				{
 					err.addProblem ( fFieldName, errMsg );
 				}
 				else
 				{
 					final String val = form.getValue ( fFieldName );
 					if ( val != null && val.length () == 0 )
 					{
 						err.addProblem ( fFieldName, errMsg );
 					}
 				}
 			}
 		} );
 	}
 
 	public webHtmlFormFieldInfo oneOf ( final String[] values, final String errMsg )
 	{
 		return validateWith ( new webHtmlFormValidationStep ()
 		{
 			@Override
 			public void validate ( handlingContext context, webHtmlFormPostWrapper form, webHtmlFormFieldInfo field, webHtmlFormValidationError err )
 			{
 				if ( !form.hasParameter ( fFieldName ) )
 				{
 					err.addProblem ( fFieldName, errMsg );
 				}
 				else
 				{
 					final String val = form.getValue ( fFieldName );
 					
 					boolean found = false;
 					for ( String v : values )
 					{
 						if ( v.equals ( val ) )
 						{
 							found = true;
 							break;
 						}
 					}
 					
 					if ( !found )
 					{
 						err.addProblem ( fFieldName, errMsg );
 					}
 				}
 			}
 		} );
 	}
 
	public webHtmlFormFieldInfo oneOf ( final Object[] values, final String errMsg )
	{
		int current = 0;
		final String[] stringVals = new String [ values.length ];
		for ( Object o : values )
		{
			stringVals [ current++ ] = o.toString ();
		}
		return oneOf ( stringVals, errMsg );
	}

 	public webHtmlFormFieldInfo matches ( final String regex, final String errMsg )
 	{
 		return validateWith ( new webHtmlFormValidationStep ()
 		{
 			@Override
 			public void validate ( handlingContext context, webHtmlFormPostWrapper form, webHtmlFormFieldInfo field, webHtmlFormValidationError err )
 			{
 				String value = form.getValue ( fFieldName );
 				if ( value == null )
 				{
 					value = "";
 				}
 				if ( !value.matches ( regex ) )
 				{
 					err.addProblem ( fFieldName, errMsg );
 				}
 			}
 		} );
 	}
 
 	public webHtmlFormFieldInfo defaultValue ( final String defVal )
 	{
 		return validateWith ( new webHtmlFormValidationStep ()
 		{
 			@Override
 			public void validate ( handlingContext context, webHtmlFormPostWrapper form, webHtmlFormFieldInfo field, webHtmlFormValidationError err )
 			{
				if ( !form.hasParameter ( fFieldName ) ||
					( form.isFormField ( fFieldName ) && form.getValue ( fFieldName ).length () == 0 ) )
 				{
 					form.changeValue ( fFieldName, defVal );
 				}
 			}
 		} );
 	}
 
	public webHtmlFormFieldInfo defaultValue ( final Object o )
	{
		return defaultValue ( o.toString () );
	}

 	// FIXME: remove once main drumlin dev is committed
 	public webHtmlFormFieldInfo checkInboxActive ( final String dummy1, String dummy2, String dummy3 )
 	{
 		return validateWith ( new webHtmlFormValidationStep ()
 		{
 			@Override
 			public void validate ( handlingContext context, webHtmlFormPostWrapper form, webHtmlFormFieldInfo field, webHtmlFormValidationError err )
 			{
 			}
 		} );
 	}
 
 	public final String fFieldName;
 	private LinkedList<webHtmlFormValidationStep> fSteps;
 }
