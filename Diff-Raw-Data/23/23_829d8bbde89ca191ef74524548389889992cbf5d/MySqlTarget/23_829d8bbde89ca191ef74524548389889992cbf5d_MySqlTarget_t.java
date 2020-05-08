 package com.bigcommerce.eg.target;
 
 import java.io.FileWriter;
 
 import java.util.List;
 
 import com.bigcommerce.eg.GenerationException;
 import com.bigcommerce.eg.ast.Attribute;
 import com.bigcommerce.eg.ast.Entity;
 import com.bigcommerce.eg.ast.IntAttribute;
 import com.bigcommerce.eg.ast.Model;
 import com.bigcommerce.eg.ast.StringAttribute;
 
 public class MySqlTarget extends AbstractTarget {
 
 	@Override
 	public void generate(Model model, String outputDirectory)
 			throws GenerationException {
 		String sqlOutputName = FilenameHelper.getSimpleFilename(model, "sql", outputDirectory);
 		
 		String modelIdentifier = model.getIdentifier();
 		
 		StringBuilder sql = new StringBuilder("CREATE DATABASE ");
 		
 		sql
 			.append(modelIdentifier)
 			.append(';')
 			.append(lineSeparator)
 			.append("USE ")
 			.append(modelIdentifier)
 			.append(';')
 			.append(lineSeparator);
 		
 		for (Entity entity : model.getEntities()) {
 			sql.append(generateCreateTable(entity));
 		}
 		
 		try {
 	        FileWriter outputStream = new FileWriter(sqlOutputName);
 	        outputStream.write(sql.toString());
 	        outputStream.close();
         } catch (Exception e) {
         	throw new GenerationException("Could not write the SQL", e);
         }
 		
 	}
 	
 	protected String generateCreateTable(Entity entity) {
 		StringBuilder create = new StringBuilder("CREATE TABLE ");
 		create.append(entity.getIdentifier());
 		
 		List<Attribute> attributes = entity.getAttributes();
		if (attributes.size() > 0) {
 			
 			create
 				.append("(")
 				.append(lineSeparator);
 			
 			boolean isFirst = true;
 			for (Attribute attribute : attributes) {
 				if (!isFirst) {
 					create
 						.append(",")
 						.append(lineSeparator);
 				}
 				create.append(generateColumn(attribute));
 				if (isFirst) {
 					isFirst = false;
 				}
 			}
 			create
 				.append(lineSeparator)
 				.append(");");
 		}
 		return create.toString();
 	}
 	
 	protected String generateColumn(Attribute attribute) {
 		if (attribute instanceof IntAttribute) {
 			return "  " + generateColumn((IntAttribute)attribute);
 		} else if (attribute instanceof StringAttribute) {
 			return "  " + generateColumn((StringAttribute)attribute);
 		} else {
 			return "";
 		}
 	}
 	
 	protected String generateColumn(IntAttribute attribute) {
 		StringBuilder column = new StringBuilder(attribute.getIdentifier());
 		column
 			.append(" INT")
 			.append(generateColumnModifiers(attribute));
 		return column.toString();
 	}
 	
 	protected String generateColumn(StringAttribute attribute) {
 		StringBuilder column = new StringBuilder(attribute.getIdentifier());
 		column.append(" VARCHAR");
 		if (attribute.hasSize()) {
 			column
 				.append('(')
 				.append(attribute.getSize())
 				.append(')');
 		}
 		if (attribute.hasDefaultValue()) {
 			column
 				.append(" DEFAULT '")
 				.append(attribute.getDefaultValue())
 				.append('\'');
 		}
 		column.append(generateColumnModifiers(attribute));
 		return column.toString();
 	}
 	
 	protected String generateColumnModifiers(Attribute attribute) {
 		StringBuilder modifiers = new StringBuilder();
 		
 		if (attribute.isRequired()) {
 			modifiers.append(" NOT NULL");
 		}
 		if (attribute.getIdentifier().equals("id")) {
 			modifiers.append(" PRIMARY KEY AUTO_INCREMENT");
 		}
 		return modifiers.toString();
 	}
 	
 	
 	
 }
