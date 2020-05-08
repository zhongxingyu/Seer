 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Objects;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
 
 import org.apache.commons.io.IOUtils;
 import org.hibernate.HibernateException;
 import org.hibernate.engine.spi.SessionImplementor;
 import org.hibernate.usertype.UserType;
 
 public class JaxbUserType<T> implements UserType {
 
   private static final int[] SQL_TYPES = { Types.LONGVARCHAR };
 
   private static JAXBContext jaxbContext;
  private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
 
   static {
     try {
       jaxbContext = JAXBContext.newInstance("org.ogf.schemas.nsi._2011._10.connection.types");
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   private final QName xmlRootElementName;
   private final Class<T> type;
 
   protected JaxbUserType(QName xmlRootElementName, Class<T> type) {
     this.xmlRootElementName = xmlRootElementName;
     this.type = type;
   }
 
   @Override
   public int[] sqlTypes() {
     return SQL_TYPES;
   }
 
   @SuppressWarnings("rawtypes")
   @Override
   public Class returnedClass() {
     return type;
   }
 
   @Override
   public boolean equals(Object x, Object y) throws HibernateException {
     return Objects.equals(x, y);
   }
 
   @Override
   public int hashCode(Object x) throws HibernateException {
     return Objects.hashCode(x);
   }
 
   @Override
   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
       throws HibernateException, SQLException {
     String string = rs.getString(names[0]);
     if (rs.wasNull()) {
       return null;
     }
 
     return fromXmlString(string);
   }
 
   T fromXmlString(String string) {
     if (string == null) {
       return null;
     }
     try (InputStream input = IOUtils.toInputStream(string, "UTF-8")) {
      XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
      try {
        return jaxbContext.createUnmarshaller().unmarshal(reader, type).getValue();
      } finally {
        reader.close();
      }
    } catch (JAXBException | IOException | XMLStreamException e) {
      throw new HibernateException(e);
     }
   }
 
   @Override
   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
       throws HibernateException, SQLException {
     String string = toXmlString(value);
     st.setString(index, string);
   }
 
   String toXmlString(Object value) {
     if (value == null) {
       return null;
     }
     try (StringWriter writer = new StringWriter()) {
       jaxbContext.createMarshaller().marshal(new JAXBElement<>(xmlRootElementName, type, type.cast(value)), writer);
       return writer.toString();
    } catch (JAXBException | IOException e) {
      throw new HibernateException(e);
     }
   }
 
   @Override
   public Object deepCopy(Object value) throws HibernateException {
     return fromXmlString(toXmlString(value));
   }
 
   @Override
   public boolean isMutable() {
     return true;
   }
 
   @Override
   public Serializable disassemble(Object value) throws HibernateException {
     return toXmlString(value);
   }
 
   @Override
   public Object assemble(Serializable cached, Object owner) throws HibernateException {
     return cached;
   }
 
   @Override
   public Object replace(Object original, Object target, Object owner) throws HibernateException {
     return deepCopy(original);
   }
 }
