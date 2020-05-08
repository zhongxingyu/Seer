 /*
  * Copyright (c) 2011 by Martin Simons.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.lunikon.rethul.model;
 
 import java.util.Locale;
 
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.annotations.NaturalId;
 
 /**
  * LocalizedString.
  * 
  * @author Martin Simons
  */
 @Entity
 public class LocalizedString
 		extends LongIdentifiedItem
 {
 	/**
 	 * The file.
 	 */
 	@ManyToOne(optional = false)
 	@NaturalId
 	private File file;
 
 	/**
 	 * The key.
 	 */
 	@NaturalId
	@Column(name = "stringKey")
 	private String key;
 
 	/**
 	 * The locale.
 	 */
 	@NaturalId
 	private Locale locale;
 
 	/**
 	 * The translation.
 	 */
 	private String translation;
 
 	/**
 	 * The pending.
 	 */
 	private boolean pending;
 
 	/**
	 * Constructs a new instance of this class.
	 */
	public LocalizedString()
	{
		// for persitence
	}

	/**
	 * Constructs a new master key. Master keys have no locale and are never
	 * "pending".
	 * 
	 * @param file
	 *            The file the key belongs to.
	 * @param key
	 *            The key string.
	 * @param translation
	 *            The master text.
	 */
	public LocalizedString(File file, String key, String translation)
	{
		super();
		this.file = file;
		this.key = key;
		this.translation = translation;
		this.pending = false;
	}

	/**
 	 * Returns the file.
 	 * 
 	 * @return the file
 	 */
 	public File getFile()
 	{
 		return file;
 	}
 
 	/**
 	 * Sets the file.
 	 * 
 	 * @param file
 	 *            the file to set
 	 */
 	public void setFile(File file)
 	{
 		this.file = file;
 	}
 
 	/**
 	 * Returns the key.
 	 * 
 	 * @return the key
 	 */
 	public String getKey()
 	{
 		return key;
 	}
 
 	/**
 	 * Sets the key.
 	 * 
 	 * @param key
 	 *            the key to set
 	 */
 	public void setKey(String key)
 	{
 		this.key = key;
 	}
 
 	/**
 	 * Returns the locale.
 	 * 
 	 * @return the locale
 	 */
 	public Locale getLocale()
 	{
 		return locale;
 	}
 
 	/**
 	 * Sets the locale.
 	 * 
 	 * @param locale
 	 *            the locale to set
 	 */
 	public void setLocale(Locale locale)
 	{
 		this.locale = locale;
 	}
 
 	/**
 	 * Returns the translation.
 	 * 
 	 * @return the translation
 	 */
 	public String getTranslation()
 	{
 		return translation;
 	}
 
 	/**
 	 * Sets the translation.
 	 * 
 	 * @param translation
 	 *            the translation to set
 	 */
 	public void setTranslation(String translation)
 	{
 		this.translation = translation;
 	}
 
 	/**
 	 * Returns the pending.
 	 * 
 	 * @return the pending
 	 */
 	public boolean isPending()
 	{
 		return pending;
 	}
 
 	/**
 	 * Sets the pending.
 	 * 
 	 * @param pending
 	 *            the pending to set
 	 */
 	public void setPending(boolean pending)
 	{
 		this.pending = pending;
 	}
 }
