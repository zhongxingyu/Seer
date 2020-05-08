 /* @formatter:off */
 /******************************************************************************
  * ELEC5616
  * Computer and Network Security, The University of Sydney
  * Copyright (C) 2002-2004, Matt Barrie, Stephen Gould and Ryan Junee
  *
  * PACKAGE:         StealthNet
  * FILENAME:        DecryptedPacket.java
  * AUTHORS:         Matt Barrie, Stephen Gould, Ryan Junee and Joshua Spence
  * DESCRIPTION:     Implementation of a StealthNet file. This class represents
  * 					encrypted file contents.
  *
  *****************************************************************************/
 /* @formatter:on */
 
 package StealthNet;
 
 /* Import Libraries ******************************************************** */
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.KeyStore.PasswordProtection;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.spec.InvalidKeySpecException;
 import java.util.Arrays;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.management.InvalidAttributeValueException;
 
 import StealthNet.Security.HashedMessageAuthenticationCode;
 import StealthNet.Security.MessageAuthenticationCode;
 import StealthNet.Security.PasswordEncryption;
 
 /* StealthNet.EncryptedFile Class Definition ******************************* */
 
 /**
  * A class to read and write encrypted files. The encrypted files are protected
  * with a password using {@link PasswordEncryption}.
  * 
  * @author Joshua Spence
  * @see PasswordProtection
  */
 public class EncryptedFile {
 	/* Debug options. */
 	private static final boolean DEBUG_FILE_IO = Debug.isDebug("StealthNet.EncryptedFile.FileIO");
 	
 	/** The algorithm to use for the {@link MessageDigest}. */
 	public static final String HASH_ALGORITHM = "SHA1";
 	
 	/** The salt to use for the encrption/decryption of the file. */
 	private byte[] salt;
 	
 	/** A hash of the password for password verification. */
 	private byte[] passwordHash;
 	
 	/** The (encrypted) data contained in the file. */
 	private byte[] data;
 	
 	/** The {@link MessageAuthenticationCode} used to provide a message digest. */
 	private byte[] digest;
 	
 	/**
 	 * If the file cannot be properly parsed, then this will be true. The file
 	 * has probably been modified or corrupted.
 	 */
 	private boolean corrupt = false;
 	
 	/** The name of the file. */
 	private String filename;
 	
 	/** The password with which to attempt to encrypt/decrypt the file. */
 	private final String password;
 	
 	/** The class to use to encrypt/decrypt the file. */
 	private PasswordEncryption encryption;
 	
 	/** The class to use to generate a MAC. */
 	private MessageAuthenticationCode mac;
 	
 	/**
 	 * Constructor to read an existing encrypted file from the file system. If
 	 * the file is corrupted then this exception won't be thrown until an
 	 * attempt is made to decrypted the file.
 	 * 
 	 * @param file The encrypted file to read.
 	 * @param password The password to use to attempt to decrypt the file.
 	 * 
 	 * @throws IOException
 	 * @throws NoSuchPaddingException
 	 * @throws InvalidAlgorithmParameterException
 	 * @throws InvalidKeySpecException
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeyException
 	 */
 	public EncryptedFile(final File file, final String password) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException {
 		/* PParse the file. */
 		final FileInputStream fileInputStream = new FileInputStream(file);
 		final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
 		final DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
 		
 		filename = file.getName();
 		this.password = password;
 		
 		try {
 			/* Read the salt. */
 			final int saltBytes = dataInputStream.readInt();
 			salt = new byte[saltBytes];
 			dataInputStream.read(salt);
 			
 			/* Read the password hash. */
 			final int hashBytes = dataInputStream.readInt();
 			passwordHash = new byte[hashBytes];
 			dataInputStream.read(passwordHash);
 			
 			/* Read the data. */
 			final int dataBytes = dataInputStream.readInt();
 			data = new byte[dataBytes];
 			dataInputStream.read(data);
 			
 			/* Read the digest. */
 			final int digestBytes = dataInputStream.readInt();
 			digest = new byte[digestBytes];
 			dataInputStream.read(digest);
 			
 			/* Setup encryption and MAC generation. */
 			encryption = new PasswordEncryption(salt, this.password);
 			mac = new HashedMessageAuthenticationCode(encryption.getSecretKey());
 		} catch (final Exception e) {
 			corrupt = true;
 			salt = null;
 			passwordHash = null;
 			data = null;
 			digest = null;
 			encryption = null;
 			mac = null;
 		} finally {
 			/* Clean up. */
 			dataInputStream.close();
 			bufferedInputStream.close();
 			fileInputStream.close();
 		}
 	}
 	
 	/**
 	 * Constructor to read an existing encrypted file from the file system.
 	 * 
 	 * @param file The encrypted file to read.
 	 * @param password The password to use to attempt to decrypt the file.
 	 * 
 	 * @throws IOException
 	 * @throws NoSuchPaddingException
 	 * @throws InvalidAlgorithmParameterException
 	 * @throws InvalidKeySpecException
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeyException
 	 */
 	public EncryptedFile(final URL file, final String password) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException {
 		/* Parse the file. */
 		final InputStream inputStream = file.openStream();
 		final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
 		final DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
 		
 		filename = file.getFile();
 		this.password = password;
 		
 		try {
 			/* Read the salt. */
 			final int saltBytes = dataInputStream.readInt();
 			salt = new byte[saltBytes];
 			dataInputStream.read(salt);
 			
 			/* Read the password hash. */
 			final int hashBytes = dataInputStream.readInt();
 			passwordHash = new byte[hashBytes];
 			dataInputStream.read(passwordHash);
 			
 			/* Read the data. */
 			final int dataBytes = dataInputStream.readInt();
 			data = new byte[dataBytes];
 			dataInputStream.read(data);
 			
 			/* Read the digest. */
 			final int digestBytes = dataInputStream.readInt();
 			digest = new byte[digestBytes];
 			dataInputStream.read(digest);
 			
 			/* Setup encryption and MAC generation. */
 			encryption = new PasswordEncryption(salt, this.password);
 			mac = new HashedMessageAuthenticationCode(encryption.getSecretKey());
 		} catch (final Exception e) {
 			corrupt = true;
 			salt = null;
 			passwordHash = null;
 			data = null;
 			digest = null;
 			encryption = null;
 			mac = null;
 		} finally {
 			/* Clean up. */
 			dataInputStream.close();
 			bufferedInputStream.close();
 			inputStream.close();
 		}
 	}
 	
 	/**
 	 * Constructor to create a new encrypted file from raw unencrypted data.
 	 * 
 	 * @param decryptedData The unencrypted data to be written to the encrypted
 	 *        file.
 	 * @param password The password to use to encrypt the file.
 	 * 
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException
 	 * @throws BadPaddingException
 	 * @throws IllegalBlockSizeException
 	 * @throws NoSuchPaddingException
 	 * @throws InvalidAlgorithmParameterException
 	 * @throws InvalidKeySpecException
 	 * @throws InvalidKeyException
 	 * @throws InvalidAttributeValueException
 	 * @throws NoSuchProviderException
 	 */
 	public EncryptedFile(final byte[] decryptedData, final String password) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidAttributeValueException, NoSuchProviderException {
 		filename = null;
 		this.password = password;
 		encryption = new PasswordEncryption(this.password);
 		mac = new HashedMessageAuthenticationCode(encryption.getSecretKey());
 		
 		salt = encryption.getSalt();
 		data = encryption.encrypt(decryptedData);
 		
 		/* Generate the password hash. */
 		final MessageDigest mdb = MessageDigest.getInstance(HASH_ALGORITHM);
 		passwordHash = mdb.digest(this.password.getBytes());
 		
 		/* Generate the message digest. */
 		digest = new byte[HashedMessageAuthenticationCode.DIGEST_BYTES];
 		digest = mac.createMAC(getDigestableData());
 	}
 	
 	/**
 	 * Write the encrypted file to the file system.
 	 * 
 	 * @param output The file to write the encrypted data to.
 	 * 
 	 * @throws IOException
 	 */
 	public void writeToFile(final File output) throws IOException {
 		filename = output.getName();
 		final FileOutputStream fileOutputStream = new FileOutputStream(output);
 		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
 		final DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
 		
 		if (DEBUG_FILE_IO)
 			System.out.println("Writing encrypted data to file '" + filename + "' with password '" + password + "'.");
 		
 		/* Write the salt to the file. */
 		dataOutputStream.writeInt(salt.length);
 		dataOutputStream.write(salt);
 		
 		/* Write the password hash to file. */
 		dataOutputStream.writeInt(passwordHash.length);
 		dataOutputStream.write(passwordHash);
 		
 		/* Write the (encrypted) data to file. */
 		dataOutputStream.writeInt(data.length);
 		dataOutputStream.write(data);
 		
 		/* Write the digest to file. */
 		dataOutputStream.writeInt(digest.length);
 		dataOutputStream.write(digest);
 		
 		/* Clean up. */
 		dataOutputStream.flush();
 		bufferedOutputStream.flush();
 		fileOutputStream.flush();
 		dataOutputStream.close();
 		bufferedOutputStream.close();
 		fileOutputStream.close();
 	}
 	
 	/**
 	 * Decrypt this file.
 	 * 
 	 * @return The decrypted contents of the file.
 	 * 
 	 * @throws BadPaddingException
 	 * @throws IllegalBlockSizeException
 	 * @throws UnsupportedEncodingException
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidAttributeValueException
 	 * @throws EncryptedFileException If the password supplied to decrypt the
 	 *         file is invalid or if the file has been modified.
 	 */
 	public byte[] decrypt() throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, EncryptedFileException, InvalidAttributeValueException {
 		if (DEBUG_FILE_IO)
 			System.out.println("Attempting to decrypt '" + filename + "' with password '" + password + "'.");
 		
 		if (corrupt)
 			throw new EncryptedFileException("Corrupted file.");
 		
 		/* Check the password. */
 		final MessageDigest mdb = MessageDigest.getInstance(HASH_ALGORITHM);
 		final byte[] ourPasswordHash = mdb.digest(password.getBytes());
 		if (!Arrays.equals(passwordHash, ourPasswordHash))
			throw new EncryptedFileException("Invalid password to decrypt file.");
 		
 		/* Check the digest. */
 		if (!mac.verifyMAC(getDigestableData(), digest))
 			throw new EncryptedFileException("Corrupted file.");
 		
 		if (encryption != null)
 			return encryption.decrypt(data);
 		else
 			return data;
 	}
 	
 	/**
 	 * Combine the salt, password hash and data arrays in order to create a
 	 * {@link MessageAuthenticationCode} digest.
 	 * 
 	 * @return A combined byte array containing the salt, password hash and
 	 *         data.
 	 */
 	private byte[] getDigestableData() {
 		final byte[] combined = new byte[salt.length + passwordHash.length + data.length];
 		
 		System.arraycopy(salt, 0, combined, 0, salt.length);
 		System.arraycopy(passwordHash, 0, combined, salt.length, passwordHash.length);
 		System.arraycopy(data, 0, combined, salt.length + passwordHash.length, data.length);
 		
 		return combined;
 	}
 }
 
 /******************************************************************************
  * END OF FILE: EncryptedFile.java
  *****************************************************************************/
