 package gbc.i18n;
 
 public interface Transcoder extends Decoder {
 
  public String fromNativeToEntities(final String input);

  public String fromEntitiesToNative(final String input);

}
