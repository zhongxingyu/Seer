 package com.github.tosdan.utils.stringhe;
 
 import java.text.SimpleDateFormat;
 import java.util.Map;
 /**
  * Validator per T-SQL
  * @author Daniele
 * @version 0.2.5-b2013-08-05
  */
 public class MapFormatTypeValidatorSQL extends MapFormatAbstractTypeValidator
 {
 	public static void main( String[] args ) throws MapFormatTypeValidatorException
 	{
 		MapFormatTypeValidatorSQL v = new MapFormatTypeValidatorSQL();
 		System.out.println( v.validate( "test del 'piffero'", "stringa" ) );
 		System.out.println( v.validate( "01012012", "compactDate" ) );
 		System.out.println( v.validate( "tRUe", "boolean" ) );
 		System.out.println( v.validate( "123", "integer" ) );
 		System.out.println( v.validate( "123.4", "numeric" ) );
 		System.out.println( v.validate( "1234", "numeric" ) );
 		System.out.println( v.validate( "test del 'piffero'", "Stirng" ) );
 	}
 	
 	@Override
 	public String getValidatorRegExPart() {
 		return "[\\p{Punct}\\p{Alnum}]*";
 	}
 	
 	/**
 	 * Effettua un opportuno controllo sul valore passato in base al tipo di dato e restituisce il valore tale com'era oppure opportunamente formattato 
 	 * @param source sorgente da validare
 	 * @param type Tipo del parametro da validare
 	 * @return La stringa sorgente normalizzata
 	 * @throws MapFormatTypeValidatorException Se la stringa sorgente non e' conforme al tipo specificato o se non e' stato specificato il parametro type.
 	 */
 	@Override
 	public String validate(String source, String type) throws MapFormatTypeValidatorException {
 		return validate( source, type, null );
 	}
 	
 	/**
 	 * Effettua un opportuno controllo sul valore passato in base al tipo di dato e restituisce il valore tale com'era oppure opportunamente formattato 
 	 * @param source sorgente da validare
 	 * @param type Tipo del parametro da validare
 	 * @param customAttributes <i>Parametro non utilizzato</i>
 	 * @return La stringa sorgente normalizzata
 	 * @throws MapFormatTypeValidatorException Se la stringa sorgente non e' conforme al tipo specificato o se non e' stato specificato il parametro type.
 	 */
 	@Override
 	public String validate( String source, String type, Map<String, Object> customAttributes ) throws MapFormatTypeValidatorException {
 		String result = "";
 
         if ( type != null ) {
         	if 		( type.equalsIgnoreCase("stringa" ) ) {
         		result = quote(source);
         	}
         	else if ( type.equalsIgnoreCase("string" ) ) {
         		result = quote(source);
         	}
         	else if ( type.equalsIgnoreCase("boolean") ) {
         		result = this.checkMatching( "(?i)true|false", source, type );
         	}
         	else if ( type.equalsIgnoreCase("integer") ) {
         		result = this.checkMatching( "[0-9]+", source, type );
         	}
         	else if ( type.equalsIgnoreCase("numeric") ) {
         		result = this.checkMatching( "([0-9])+(.([0-9])+)?", source, type );
         	}
         	else if ( type.equalsIgnoreCase("compactDate") ) {
         		setDateFormat( new SimpleDateFormat("ddMMyyyy") );
         		result = parseDate(source);
         	}
         	else if ( type.equalsIgnoreCase("revCompDate") ) {
         		setDateFormat( new SimpleDateFormat( "yyyyMMdd") );
         		result = parseDate(source);
         	}
         	else if ( type.equalsIgnoreCase("itaDate") ) {
         		setDateFormat( new SimpleDateFormat( "dd/MM/yyyy") );
         		result = parseDate(source);        		
         	}
         	else if ( type.equalsIgnoreCase("revItaDate") ) {
         		setDateFormat( new SimpleDateFormat( "yyyy/MM/dd") );
         		result = parseDate(source);        		
         	}
         	else if ( type.equalsIgnoreCase("date") ) {
         		setDateFormat( new SimpleDateFormat( "dd-MM-yyyy") );
         		result = parseDate(source);        		
         	}
         	else if ( type.equalsIgnoreCase("reverseDate") ) {
         		setDateFormat( new SimpleDateFormat( "yyyy-MM-dd") );
         		result = parseDate(source);        		
         	}
         	else if ( type.equalsIgnoreCase("table") ) {
        		result = this.checkMatching( "[a-zA-Z_@#][a-zA-Z0-9_ @#$]*", source, type );   
         	}
         	else if ( type.equalsIgnoreCase("tablePart") ) {
        		result = this.checkMatching( "[a-zA-Z0-9_ @#$]+", source, type );   
         	}
         	else if ( type.equalsIgnoreCase("orderBy") ) {
        		result = this.checkMatching( "[a-zA-Z0-9_, @#$]+", source, type );   
         	}
         	else if ( type.equalsIgnoreCase("free") ) {
         		result = source;   
         	} else
         		throw new MapFormatTypeValidatorException("Errore di validazione: il tipo di validazione '"+ type +"' e' sconosciuto. Parametro source='"+source+"'");
         	
         } else {
         	throw new MapFormatTypeValidatorException( "Parametro type mancante." );
         }
 		
 		return result;
 	}
 	
 	@Override
 	protected String quote(String input) {
 		return "'" + input.replaceAll( "'", "''" ) + "'";
 	}
 
 	
 }
