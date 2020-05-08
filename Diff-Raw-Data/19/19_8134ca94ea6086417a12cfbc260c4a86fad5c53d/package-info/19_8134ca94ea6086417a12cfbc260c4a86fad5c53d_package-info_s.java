 /**
 * The client uses a {@link FileChooser}, which can be used to select a file from the SD card of your android gadget.
 * As a private key is necessary to encrypt data received from a server the {@link FileChooser} is used to select a keyfile.
 * All servers and responding keyfiles are saved in the {@link ServerHistory} and therefore can be saved and reused after closing the client.
  * Every error is displayed by using a simple pop up window defined by the class {@link ErrorFragment}.
  */
 package cryptocast.client;
