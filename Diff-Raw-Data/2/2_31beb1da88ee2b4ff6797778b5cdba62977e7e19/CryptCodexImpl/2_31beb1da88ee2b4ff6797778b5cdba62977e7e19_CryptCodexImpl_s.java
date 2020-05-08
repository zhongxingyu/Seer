 /*
  * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
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
 package org.codeartisans.qipki.crypto.codec;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.ASN1TaggedObject;
 import org.bouncycastle.asn1.DEREncodable;
 import org.bouncycastle.asn1.DERInteger;
 import org.bouncycastle.asn1.DERString;
 import org.bouncycastle.util.encoders.Base64;
 
 import org.codeartisans.qipki.crypto.QiCryptoFailure;
 
 public class CryptCodexImpl
         implements CryptCodex
 {
 
     @Override
     public String toString( DEREncodable obj )
     {
         if ( obj instanceof DERString ) {
             return ( ( DERString ) obj ).getString();
         } else if ( obj instanceof DERInteger ) {
             return ( ( DERInteger ) obj ).getValue().toString();
         } else if ( obj instanceof ASN1TaggedObject ) {
             ASN1TaggedObject tagObj = ( ASN1TaggedObject ) obj;
             return "[" + tagObj.getTagNo() + "] " + toString( tagObj.getObject() );
         } else if ( obj instanceof ASN1Sequence ) {
             ASN1Sequence aObj = ( ASN1Sequence ) obj;
             StringBuilder sb = new StringBuilder( "[" );
             for ( int i = 0, len = aObj.size(); i < len; i++ ) {
                 sb.append( toString( aObj.getObjectAt( i ) ) );
                 if ( i != len - 1 ) {
                     sb.append( ", " );
                 }
             }
             return sb.append( "]" ).toString();
         } else {
             return obj.toString();
         }
     }
 
     @Override
     public String toHexString( BigInteger bigInt )
     {
         return toHexString( bigInt.toByteArray() );
     }
 
     @Override
     public String toHexString( byte[] bytes )
     {
         StringBuilder sb = new StringBuilder();
         for ( int i = 0; i < bytes.length; i++ ) {
             int hexaValue = 0xFF & bytes[i];
            sb.append( String.format( "%02X", hexaValue ) );
         }
         return sb.toString();
     }
 
     @Override
     public String toHexString( boolean[] bools )
     {
         StringBuilder sb = new StringBuilder( bools.length );
         for ( int idx = 0; idx < bools.length; idx++ ) {
             sb.append( bools[idx] ? "1" : "0" );
         }
         return Integer.toHexString( Integer.parseInt( sb.toString(), 2 ) );
     }
 
     @Override
     public String toBase64( byte[] bytes )
     {
         try {
             return new String( Base64.encode( bytes ), "UTF-8" );
         } catch ( UnsupportedEncodingException ex ) {
             throw new QiCryptoFailure( "Unable to encode data in Base64", ex );
         }
     }
 
 }
