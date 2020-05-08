 package gov.nih.nci.caXchange;
 
 public interface CaxchangeErrors {
 	public static final String AXIS_FAULT="101";
     public static final String MALFORMED_URI="201";
     public static final String REMOTE_EXCEPTION="202";
     public static final String TRANSFORMER_EXCEPTION="301";
     public static final String DESERIALIZATION_EXCEPTION="302";
     public static final String SAX_EXCEPTION="303";
     public static final String PARSER_CONFIGURATION_EXCEPTION="304";
     public static final String IO_EXCEPTION="305";
    public static final String ERROR_STORING_MESSAGE="306";
     public static final String OK="000";
     public static final String UNKNOWN="001";
 	public static final String UNHANDLED_MESSAGE_TYPE = "401";
 	public static final String CONNECT_EXCEPTION = "203";
 	public static final String TIMEOUT = "204";
 }
