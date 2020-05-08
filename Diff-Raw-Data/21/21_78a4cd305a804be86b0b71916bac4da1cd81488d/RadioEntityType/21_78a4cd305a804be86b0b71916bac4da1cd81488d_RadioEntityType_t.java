 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import javax.xml.bind.annotation.*;
 
 /**
  * Section 5.2.25. Identifies the type of radio
  *
  * Copyright (c) 2008, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
 public class RadioEntityType extends Object implements Serializable
 {
    /** Kind of entity */
    protected short  entityKind;
 
    /** Domain of entity (air, surface, subsurface, space, etc) */
    protected short  domain;
 
    /** country to which the design of the entity is attributed */
    protected int  country;
 
    /** category of entity */
    protected short  category;
 
    /** specific info based on subcategory field */
    protected short  nomenclatureVersion;
 
    protected int  nomenclature;
 
 
 /** Constructor */
  public RadioEntityType()
  {
  }
 
 public int getMarshalledSize()
 {
    int marshalSize = 0; 
 
    marshalSize = marshalSize + 1;  // entityKind
    marshalSize = marshalSize + 1;  // domain
    marshalSize = marshalSize + 2;  // country
    marshalSize = marshalSize + 1;  // category
    marshalSize = marshalSize + 1;  // nomenclatureVersion
    marshalSize = marshalSize + 2;  // nomenclature
 
    return marshalSize;
 }
 
 
 public void setEntityKind(short pEntityKind)
 { entityKind = pEntityKind;
 }
 
 @XmlAttribute
 public short getEntityKind()
 { return entityKind; 
 }
 
 public void setDomain(short pDomain)
 { domain = pDomain;
 }
 
 @XmlAttribute
 public short getDomain()
 { return domain; 
 }
 
 public void setCountry(int pCountry)
 { country = pCountry;
 }
 
 @XmlAttribute
 public int getCountry()
 { return country; 
 }
 
 public void setCategory(short pCategory)
 { category = pCategory;
 }
 
 @XmlAttribute
 public short getCategory()
 { return category; 
 }
 
 public void setNomenclatureVersion(short pNomenclatureVersion)
 { nomenclatureVersion = pNomenclatureVersion;
 }
 
 @XmlAttribute
 public short getNomenclatureVersion()
 { return nomenclatureVersion; 
 }
 
 public void setNomenclature(int pNomenclature)
 { nomenclature = pNomenclature;
 }
 
 @XmlAttribute
 public int getNomenclature()
 { return nomenclature; 
 }
 
 
 public void marshal(DataOutputStream dos)
 {
     try 
     {
        dos.writeByte( (byte)entityKind);
        dos.writeByte( (byte)domain);
        dos.writeShort( (short)country);
        dos.writeByte( (byte)category);
        dos.writeByte( (byte)nomenclatureVersion);
        dos.writeShort( (short)nomenclature);
     } // end try 
     catch(Exception e)
     { 
       System.out.println(e);}
     } // end of marshal method
 
 public void unmarshal(DataInputStream dis)
 {
     try 
     {
        entityKind = (short)dis.readUnsignedByte();
        domain = (short)dis.readUnsignedByte();
        country = (int)dis.readUnsignedShort();
        category = (short)dis.readUnsignedByte();
        nomenclatureVersion = (short)dis.readUnsignedByte();
        nomenclature = (int)dis.readUnsignedShort();
     } // end try 
    catch(Exception e)
     { 
       System.out.println(e); 
     }
  } // end of unmarshal method 
 
 
  /**
   * The equals method doesn't always work--mostly on on classes that consist only of primitives. Be careful.
   */
  public boolean equals(RadioEntityType rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (entityKind == rhs.entityKind)) ivarsEqual = false;
      if( ! (domain == rhs.domain)) ivarsEqual = false;
      if( ! (country == rhs.country)) ivarsEqual = false;
      if( ! (category == rhs.category)) ivarsEqual = false;
      if( ! (nomenclatureVersion == rhs.nomenclatureVersion)) ivarsEqual = false;
      if( ! (nomenclature == rhs.nomenclature)) ivarsEqual = false;
 
     return ivarsEqual;
  }
 } // end of class
 // Copyright (c) 1995-2009 held by the author(s).  All rights reserved.
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions
 //  are met:
 // 
 //  * Redistributions of source code must retain the above copyright
 // notice, this list of conditions and the following disclaimer.
 // * Redistributions in binary form must reproduce the above copyright
 // notice, this list of conditions and the following disclaimer
 // in the documentation and/or other materials provided with the
 // distribution.
 // * Neither the names of the Naval Postgraduate School (NPS)
 //  Modeling Virtual Environments and Simulation (MOVES) Institute
 // (http://www.nps.edu and http://www.MovesInstitute.org)
 // nor the names of its contributors may be used to endorse or
 //  promote products derived from this software without specific
 // prior written permission.
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 // CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 // LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 // ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 // POSSIBILITY OF SUCH DAMAGE.
