 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.android.preference;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.util.Map;
 
 import se.toxbee.sleepfighter.utils.prefs.BasePreferenceManager;
 import se.toxbee.sleepfighter.utils.prefs.PreferenceManager;
 import se.toxbee.sleepfighter.utils.prefs.PreferenceNode;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Base64;
 import android.util.Base64InputStream;
 import android.util.Base64OutputStream;
 
 /**
  * {@link SharedPreferenceNode} is an adapter for {@link SharedPreferences}.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Dec 14, 2013
  */
 public class SharedPreferenceManager extends BasePreferenceManager {
 	private final SharedPreferences prefs;
 	private Editor edit;
 
 	/**
 	 * Constructs a SharedPreferenceNode.
 	 *
 	 * @param prefs the {@link SharedPreferences} to adapt to.
 	 */
 	public SharedPreferenceManager( SharedPreferences prefs ) {
 		this.prefs = prefs;
 		this.edit = null;
 	}
 
 	@Override
 	public boolean contains( String key ) {
 		return this.prefs.contains( key );
 	}
 
 	@Override
 	public Map<String, ?> getAll() {
 		return this.prefs.getAll();
 	}
 
 	@Override
 	public boolean getBoolean( String key, boolean def ) {
 		return this.prefs.getBoolean( key, def );
 	}
 
 	@Override
 	public char getChar( String key, char def ) {
 		return (char) this.prefs.getInt( key, def );
 	}
 
 	@Override
 	public short getShort( String key, short def ) {
 		return (short) this.prefs.getInt( key, def );
 	}
 
 	@Override
 	public int getInt( String key, int def ) {
 		return this.prefs.getInt( key, def );
 	}
 
 	@Override
 	public long getLong( String key, long def ) {
 		return this.prefs.getLong( key, def );
 	}
 
 	@Override
 	public float getFloat( String key, float def ) {
 		return this.prefs.getFloat( key, def );
 	}
 
 	@Override
 	public double getDouble( String key, double def ) {
 		long defL = Double.doubleToRawLongBits( def );
 		return Double.longBitsToDouble( this.prefs.getLong( key, defL ) );
 	}
 
 	@Override
 	public String getString( String key, String def ) {
 		return this.prefs.getString( key, def );
 	}
 
 	@Override
 	public PreferenceNode setBoolean( String key, boolean val ) {
 		return tryac( edit(), edit.putBoolean( key, val ) );
 	}
 
 	@Override
 	public PreferenceNode setChar( String key, char val ) {
 		return this.setInt( key, val );
 	}
 
 	@Override
 	public PreferenceNode setShort( String key, short val ) {
 		return this.setInt( key, val );
 	}
 
 	@Override
 	public PreferenceNode setInt( String key, int val ) {
 		return tryac( edit(), edit.putInt( key, val ) );
 	}
 
 	@Override
 	public PreferenceNode setLong( String key, long val ) {
 		return tryac( edit(), edit.putLong( key, val ) );
 	}
 
 	@Override
 	public PreferenceNode setFloat( String key, float val ) {
 		return tryac( edit(), edit.putFloat( key, val ) );
 	}
 
 	@Override
 	public PreferenceNode setDouble( String key, double val ) {
 		return this.setLong( key, Double.doubleToRawLongBits( val ) );
 	}
 
 	@Override
 	public PreferenceNode setString( String key, String val ) {
 		return tryac( edit(), edit.putString( key, val ) );
 	}
 
 	@Override
 	public PreferenceNode remove( String key ) {
 		return tryac( edit(), edit.remove( key ) );
 	}
 
 	@Override
 	public PreferenceNode clear() {
 		return tryac( edit(), edit.clear() );
 	}
 
 	@Override
 	public boolean isApplying() {
 		return this.edit != null;
 	}
 
 	private boolean edit() {
 		boolean isNotApplying = this.edit == null;
 
 		if ( isNotApplying ) {
 			this.edit = this.prefs.edit();
 		}
 
 		return isNotApplying;
 	}
 
 	private PreferenceNode tryac( boolean autoCommit, Editor editor ) {
 		if ( autoCommit && this.isApplying() ) {
			this._apply();
 		}
 
 		return this;
 	}
 
	private void _apply() {
		this.edit.apply();
		this.edit = null;
	}

 	@Override
 	public PreferenceManager _apply( PreferenceNode node, PreferenceEditCallback cb ) {
 		this.edit();
 
 		cb.editPreference( node );
 
		this._apply();
 		return this;
 	}
 
 	@Override
 	public boolean _applyForResult( PreferenceNode node, PreferenceEditCallback cb ) {
 		this.edit();
 
 		cb.editPreference( node );
 
		boolean r = this.edit.commit();
		this.edit = null;
		return r;
 	}
 
 	@SuppressWarnings( "unchecked" )
 	@Override
 	public <U extends Serializable> U set( String key, U value ) {
 		// Remove if null.
 		if ( value == null ) {
 			this.edit.remove( key );
 			return this.get( key, null );
 		}
 
 		// If we've got a "primitive", redirect to correct setter.
 		if ( value.getClass().isPrimitive() ) {
 			Object old = null;
 			
 			if ( value instanceof Boolean ) {
 				old = this.contains( key ) ? this.getBoolean( key, false ) : null;
 				this.setBoolean( key, (Boolean) value );
 			} else if ( value instanceof Character ) {
 				old = this.contains( key ) ? this.getChar( key, (char) 0 ) : null;
 				this.setChar( key, (Character) value );
 			} else if ( value instanceof Short ) {
 				old = this.contains( key ) ? this.getShort( key, (short) 0 ) : null;
 				this.setShort( key, (Short) value );
 			} else if ( value instanceof Integer ) {
 				old = this.contains( key ) ? this.getInt( key, 0 ) : null;
 				this.setInt( key, (Integer) value );
 			} else if ( value instanceof Long ) {
 				old = this.contains( key ) ? this.getLong( key, 0 ) : null;
 				this.setLong( key, (Long) value );
 			} else if ( value instanceof Float ) {
 				old = this.contains( key ) ? this.getFloat( key, 0 ) : null;
 				this.setFloat( key, (Float) value );
 			} else if ( value instanceof Double ) {
 				old = this.contains( key ) ? this.getDouble( key, 0 ) : null;
 				this.setDouble( key, (Double) value );
 			}
 
 			return (U) old;
 		} else if ( value instanceof String ) {
 			Object old = this.contains( key ) ? this.getString( key, null ) : null;
 			this.setString( key, (String) value );
 			return (U) old;
 		}
 
 		// Convert the value to an object.
 	    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
 		ObjectOutputStream objectOutput;
 		try {
 			objectOutput = new ObjectOutputStream( arrayOutputStream );
 			objectOutput.writeObject( value );
 			byte[] data = arrayOutputStream.toByteArray();
 			objectOutput.close();
 			arrayOutputStream.close();
 
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			Base64OutputStream b64 = new Base64OutputStream( out, Base64.DEFAULT );
 			b64.write( data );
 			b64.close();
 			out.close();
 
 			U old = this.get( key, null );
 			this.setString( key, new String( out.toByteArray() ) );
 			return old;
 		} catch ( IOException e ) {
 			throw new RuntimeException( e );
 		}
 	}
 
 	@SuppressWarnings( "unchecked" )
 	@Override
 	public <U extends Serializable> U get( String key, U def ) {
 		// If we've got a "primitive", redirect to correct getter.
 		if ( def != null ) {
 			if ( def.getClass().isPrimitive() ) {
 				Object retr = null;
 				if ( def instanceof Boolean ) {
 					retr = this.getBoolean( key, (Boolean) def );
 				} else if ( def instanceof Character ) {
 					retr = this.getChar( key, (Character) def );
 				} else if ( def instanceof Short ) {
 					retr = this.getShort( key, (Short) def );
 				} else if ( def instanceof Integer ) {
 					retr = this.getInt( key, (Integer) def );
 				} else if ( def instanceof Long ) {
 					retr = this.getLong( key, (Long) def );
 				} else if ( def instanceof Float ) {
 					retr = this.getFloat( key, (Float) def );
 				} else if ( def instanceof Double ) {
 					retr = this.getDouble( key, (Double) def );
 				}
 				return (U) retr;
 			} else if ( def instanceof String ) {
 				return (U) this.getString( key, (String) def );
 			}
 		}
 
 		String str = this.prefs.getString( key, null );
 		if ( str == null ) {
 			return def;
 		}
 
 		byte[] bytes = str.getBytes();
 		if ( bytes.length == 0 ) {
 			return def;
 		}
 
 		U r = def;
 
 		// Read the value.
 		ObjectInputStream in = null;
 		try {
 			ByteArrayInputStream byteArray = new ByteArrayInputStream( bytes );
 			Base64InputStream base64InputStream = new Base64InputStream( byteArray, Base64.DEFAULT );
 			in = new ObjectInputStream( base64InputStream );
 			r = (U) in.readObject();
 		} catch ( ObjectStreamException e ) {
 			e.printStackTrace();
 		} catch ( ClassNotFoundException e ) {
 			e.printStackTrace();
 		} catch ( IOException e ) {
 			e.printStackTrace();
 		} finally {
 			try {
 				in.close();
 			} catch ( IOException e ) {
 				e.printStackTrace();
 			}
 		}
 
 		return r;
 	}
 }
