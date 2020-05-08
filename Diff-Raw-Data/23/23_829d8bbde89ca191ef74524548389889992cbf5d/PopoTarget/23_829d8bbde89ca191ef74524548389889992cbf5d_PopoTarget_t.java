 package com.bigcommerce.eg.target;
 
 import java.io.FileWriter;
 import java.util.List;
 
 import com.bigcommerce.eg.GenerationException;
 import com.bigcommerce.eg.ast.Attribute;
 import com.bigcommerce.eg.ast.Entity;
 import com.bigcommerce.eg.ast.Model;
 import static com.google.common.base.CaseFormat.*;
 
 public class PopoTarget extends AbstractTarget {
 
 	@Override
 	public void generate(Model model, String outputDirectory)
 			throws GenerationException {
 
 		String namespace = getNamespace(model);
 		for (Entity entity : model.getEntities()) {
 			String popoOutputName = getPopoFilename(entity, outputDirectory);
 			String popo = generatePopo(entity, namespace);
 			try {
 		        FileWriter outputStream = new FileWriter(popoOutputName);
 		        outputStream.write(popo.toString());
 		        outputStream.close();
 	        } catch (Exception e) {
 	        	throw new GenerationException("Could not write the POPO", e);
 	        }
 		}
 		
 
 	}
 	
 	protected String generatePopo(Entity entity, String namespace) {
 		StringBuilder popo = new StringBuilder("<?php");
 		
 		List<Attribute> attributes = entity.getAttributes();
 		
 		popo.append(lineSeparator)
 			.append(lineSeparator)
 			.append("namespace ")
 			.append(namespace)
 			.append(";")
 			.append(lineSeparator)
 			.append(lineSeparator)
 			.append("class ")
 			.append(LOWER_UNDERSCORE.to(UPPER_CAMEL, entity.getIdentifier()))
 			.append(" {")
 			.append(lineSeparator)
 			.append(lineSeparator);
 		
 		for (Attribute attribute : attributes) {
 			popo.append(generateProperty(attribute))
 				.append(lineSeparator);
 		}
 		popo.append(lineSeparator);
 		
 		for (Attribute attribute : attributes) {
 			popo.append(generateGetter(attribute))
 				.append(lineSeparator)
 				.append(lineSeparator)
 				.append(generateSetter(attribute))
 				.append(lineSeparator)
 				.append(lineSeparator);
 		}
 		
		popo.append('}');
 
 		return popo.toString();
 	}
 	
 	protected String generateProperty(Attribute attribute) {
 		StringBuilder property = new StringBuilder("\tprotected $");
 		property
 			.append(LOWER_UNDERSCORE.to(LOWER_CAMEL, attribute.getIdentifier()))
 			.append(';');
 		return property.toString();
 	}
 	
 	protected String generateGetter(Attribute attribute) {
 		String upperCamelIdentifier = LOWER_UNDERSCORE.to(UPPER_CAMEL, attribute.getIdentifier());
 		String lowerCamelIdentifier = LOWER_UNDERSCORE.to(LOWER_CAMEL, attribute.getIdentifier());
 		StringBuilder getter = new StringBuilder("\tpublic function get");
 		getter
 			.append(upperCamelIdentifier)
 			.append("()")
 			.append(lineSeparator)
 			.append("\t{")
 			.append(lineSeparator)
 			.append("\t\treturn $this->")
 			.append(lowerCamelIdentifier)
 			.append(';')
 			.append(lineSeparator)
 			.append("\t}");
 		return getter.toString();
 	}
 	
 	protected String generateSetter(Attribute attribute) {
 		String upperCamelIdentifier = LOWER_UNDERSCORE.to(UPPER_CAMEL, attribute.getIdentifier());
 		String lowerCamelIdentifier = LOWER_UNDERSCORE.to(LOWER_CAMEL, attribute.getIdentifier());
 		StringBuilder getter = new StringBuilder("\tpublic function set");
 		getter
 			.append(upperCamelIdentifier)
 			.append("($")
 			.append(lowerCamelIdentifier)
 			.append(')')
 			.append(lineSeparator)
 			.append("\t{")
 			.append(lineSeparator)
 			.append("\t\t$this->")
 			.append(lowerCamelIdentifier)
 			.append(" = $")
 			.append(lowerCamelIdentifier)
 			.append(';')
 			.append(lineSeparator)
 			.append("\t\treturn $this;")
 			.append(lineSeparator)
 			.append("\t}");
 		return getter.toString();
 	}
 	
 	protected String getNamespace(Model model) {
 		String modelIdentifier = model.getIdentifier();
 		return LOWER_UNDERSCORE.to(UPPER_CAMEL, modelIdentifier);
 	}
 	
 	protected String getPopoFilename(Entity entity, String outputDirectory) {
 		return outputDirectory + '/' + LOWER_UNDERSCORE.to(UPPER_CAMEL, entity.getIdentifier()) + ".php";
 	}
 
 }
