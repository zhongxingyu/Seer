 package br.maplink.webservices.places.client.converter;
 
 import java.util.GregorianCalendar;
 
 public interface DateConverter {
 
	public abstract String toRfc1132(GregorianCalendar date);
 
 }
