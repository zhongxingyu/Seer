 package org.vamdc.portal.session.queryBuilder.unitConv;
 
import java.io.Serializable;

public interface Converter extends Serializable{
 	public Double convert(Double value);
 }
