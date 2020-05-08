 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.test.classloading.vfs.metadata.xml;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
 
 import org.jboss.xb.binding.sunday.unmarshalling.AllBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.AttributeBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.ChoiceBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.ModelGroupBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.ParticleBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.SequenceBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.TermBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.WildcardBinding;
 
 /**
  * SchemaPrinter.
  *
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class SchemaPrinter
 {
    @SuppressWarnings("unchecked")
    public static String printSchema(SchemaBinding schemaBinding)
    {
       String nsURI = (String) schemaBinding.getNamespaces().iterator().next();
       StringBuilder builder = new StringBuilder();
       builder.append("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n");
       if (XMLConstants.NULL_NS_URI.equals(nsURI) == false)
       {
          builder.append("            targetNamespace=\"").append(nsURI).append("\"\n");
          builder.append("            xmlns=\"").append(nsURI).append("\"\n");
       }
       builder.append(">\n");
       int pad=3;
       Iterator<ParticleBinding> elements = schemaBinding.getElementParticles();
       while (elements != null && elements.hasNext())
          printElement(builder, pad, elements.next());
       Iterator<TypeBinding> types = schemaBinding.getTypes();
       while (types != null && types.hasNext())
          printType(builder, pad, types.next());
       builder.append("</xsd:schema>");
       return builder.toString();
    }
 
    public static void printElement(StringBuilder builder, int pad, ParticleBinding particle)
    {
       ElementBinding element = (ElementBinding) particle.getTerm();
       pad(builder, pad);
       builder.append("<element name=\"").append(element.getQName()).append("\"");
       int minOccurs = particle.getMinOccurs();
       if (minOccurs != 1)
          builder.append(" minOccurs=\"").append(minOccurs).append("\"");
       int maxOccurs = particle.getMaxOccurs();
       if (maxOccurs != 1)
       {
          builder.append(" maxOccurs=\"");
          if (maxOccurs == -1)
             builder.append("unbounded");
          else
             builder.append(maxOccurs);
          builder.append("\"");
       }
       TypeBinding type = element.getType();
       if (type != null && type.getQName() != null)
          builder.append(" type=\"").append(type.getQName()).append("\"");
       builder.append(">");
       if (type != null && type.getQName() == null)
          printAnonymousType(builder, pad + 3, type);
       pad(builder, pad);
       builder.append("</element>");
    }
 
    public static void printAnonymousType(StringBuilder builder, int pad, TypeBinding type)
    {
       pad(builder, pad);
       builder.append("<complexType>");
       TypeBinding baseTypeBinding = type.getBaseType();
       String qName;
       if (baseTypeBinding != null)
          qName = baseTypeBinding.getQName().toString();
       else
          qName = "xsd:anyType";
       pad(builder, pad);
       builder.append("   <restriction base=\"").append(qName).append("\">");
       printTypeInfo(builder, pad+6, type);
       pad(builder, pad);
       builder.append("   </restriction>");
       pad(builder, pad);
       builder.append("</complexType>");
    }
 
    public static void printType(StringBuilder builder, int pad, TypeBinding type)
    {
      QName name = type.getQName();
      if (name != null)
      {
         if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI()))
            return;
      }
       if (type.isSimple())
          printSimpleType(builder, pad, type);
       else
          printComplexType(builder, pad, type);
    }
 
    public static void printSimpleType(StringBuilder builder, int pad, TypeBinding type)
    {
       pad(builder, pad);
       builder.append("<simpleType name=\"").append(type.getQName()).append("\"");
       TypeBinding baseTypeBinding = type.getBaseType();
       String qName = null;
       if (baseTypeBinding != null)
          qName = baseTypeBinding.getQName().toString();
       else
          qName = "xsd:anySimpleType";
       builder.append(" base=\"").append(qName).append("\">");
       printTypeInfo(builder, pad+3, type);
       pad(builder, pad);
       builder.append("</simpleType>");
    }
 
    public static void printComplexType(StringBuilder builder, int pad, TypeBinding type)
    {
       pad(builder, pad);
       builder.append("<complexType name=\"").append(type.getQName()).append("\"");
       TypeBinding baseTypeBinding = type.getBaseType();
       String qName = null;
       if (baseTypeBinding != null)
          qName = baseTypeBinding.getQName().toString();
       else
          qName = "xsd:anyType";
       builder.append(" base=\"").append(qName).append("\">");
       printTypeInfo(builder, pad+3, type);
       pad(builder, pad);
       builder.append("</complexType>");
    }
 
    @SuppressWarnings("unchecked")
    public static void printTypeInfo(StringBuilder builder, int pad, TypeBinding type)
    {
       ParticleBinding particle = type.getParticle();
       if (particle != null)
       {
          printParticle(builder, pad, particle);
       }
 
       Collection<AttributeBinding> attributes = type.getAttributes();
       if (attributes != null)
       {
          for (AttributeBinding attribute : attributes)
          {
             printAttribute(builder, pad, attribute);
          }
       }
    }
 
    public static void printParticle(StringBuilder builder, int pad, ParticleBinding particle)
    {
       TermBinding term = particle.getTerm();
       if (term instanceof ElementBinding)
          printElement(builder, pad, particle);
       else if (term instanceof WildcardBinding)
          printWildcard(builder, pad, particle);
       else if (term instanceof SequenceBinding)
          printModel(builder, pad, particle, "sequence");
       else if (term instanceof AllBinding)
          printModel(builder, pad, particle, "all");
       else if (term instanceof ChoiceBinding)
          printModel(builder, pad, particle, "choice");
    }
 
    @SuppressWarnings("unchecked")
    public static void printModel(StringBuilder builder, int pad, ParticleBinding particle, String prefix)
    {
       pad(builder, pad);
       builder.append("<").append(prefix);
       int minOccurs = particle.getMinOccurs();
       if (minOccurs != 1)
          builder.append(" minOccurs=\"").append(minOccurs).append("\"");
       int maxOccurs = particle.getMaxOccurs();
       if (maxOccurs != 1)
       {
          builder.append(" maxOccurs=\"");
          if (maxOccurs == -1)
             builder.append("unbounded");
          else
             builder.append(maxOccurs);
          builder.append("\"");
       }
       builder.append(">");
       ModelGroupBinding model = (ModelGroupBinding) particle.getTerm();
       Collection<ParticleBinding> particles = model.getParticles();
       boolean newLine = true;
       if (particles != null)
       {
          for (ParticleBinding component : particles)
             printParticle(builder, pad+3, component);
       }
       else
       {
          newLine = false;
       }
       pad(builder, pad, newLine);
       builder.append("</").append(prefix).append(">");
    }
 
    public static void printWildcard(StringBuilder builder, int pad, ParticleBinding particle)
    {
       pad(builder, pad);
       builder.append("<any");
       int minOccurs = particle.getMinOccurs();
       if (minOccurs != 1)
          builder.append(" minOccurs=\"").append(minOccurs).append("\"");
       int maxOccurs = particle.getMaxOccurs();
       if (maxOccurs != 1)
       {
          builder.append(" maxOccurs=\"");
          if (maxOccurs == -1)
             builder.append("unbounded");
          else
             builder.append(maxOccurs);
          builder.append("\"");
       }
       builder.append("/>");
    }
 
    public static void printAttribute(StringBuilder builder, int pad, AttributeBinding attribute)
    {
       pad(builder, pad);
       TypeBinding typeBinding = attribute.getType();
       String qName = null;
       if (typeBinding != null && typeBinding.getQName() != null)
          qName = typeBinding.getQName().toString();
       else
          qName = "CDATA";
       builder.append("<attribute name=\"").append(attribute.getQName().getLocalPart()).append("\"");
       builder.append(" type=\"").append(qName).append("\"");
       if (attribute.getRequired() == false)
          builder.append(" optional=\"true\"/>");
    }
 
    public static void pad(StringBuilder builder, int pad)
    {
       pad(builder, pad, true);
    }
 
    public static void pad(StringBuilder builder, int pad, boolean newLine)
    {
       if (newLine)
          builder.append("\n");
       for (int i = 0; i < pad; ++ i)
          builder.append(' ');
    }
 }
