 package net.sf.javascribe.patterns.model;
 
 import java.util.List;
 
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaBeanType;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.ServiceLocator;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5CodeSnippet;
 import net.sf.jsom.java5.Java5DeclaredMethod;
 import net.sf.jsom.java5.Java5SourceFile;
 
 @Scannable
 @Processor
 public class EntityRelationshipsProcessor {
 
 	private static final String HAS_ONE = " has a ";
 	private static final String HAS_MANY = " has many ";
 
 	@ProcessorMethod(componentClass=EntityRelationships.class)
 	public void process(EntityRelationships comp,ProcessorContext ctx) throws JavascribeException {
 
 		ctx.setLanguageSupport("Java");
 		
 		boolean orderByPk = false;
 		if (ctx.getProperty(EntityRelationships.ORDERBY_PK)!=null) {
 			orderByPk = true;
 		}
 		
 		if (comp.getEntityManager().trim().length()==0) {
 			throw new JavascribeException("EntityRelationships must have an entity manager specified.");
 		}
 		if (comp.getJpaDaoFactory().trim().length()==0) {
 			throw new JavascribeException("EntityRelationships must have a JPA Dao Factory specified.");
 		}
 		String daoFactoryName = comp.getJpaDaoFactory();
 
 		System.out.println("Processing EntityRelationships for JPA DAO Factory '"+daoFactoryName+"'");
 		List<Relationship> rels = comp.getRel();
 		if (rels.size()==0) return;
 
 		ServiceLocator daoFactoryType = (ServiceLocator)ctx.getType(daoFactoryName);
 		if (daoFactoryType==null) {
 			throw new JavascribeException("Couldn't find DAO Factory Type '"+daoFactoryName+"'");
 		}
 
 		for(Relationship rel : rels) {
 			try {
 				String r = rel.getValue();
 
 				int relType = 0; // 1 = has one, 2 = has many
 				String ownerName = null;
 				String ownedName = null;
 
 				int i = r.indexOf(HAS_ONE);
 				if (i>0) {
 					relType = 1;
 					ownerName = r.substring(0, i).trim();
 					ownedName = r.substring(i+HAS_ONE.length()).trim();
 				} else if (r.indexOf(HAS_MANY)>0) {
 					i = r.indexOf(HAS_MANY);
 					relType = 2;
 					ownerName = r.substring(0,i).trim();
 					ownedName = r.substring(i+HAS_MANY.length()).trim();
 				} else {
 					throw new JavascribeException("Could not determine relationship type from relationship string '"+r+"'");
 				}
 
 				if ((ownerName.length()==0) || (ownedName.length()==0)) {
 					throw new JavascribeException("Invalid relationship string: '"+r+"'");
 				}
 				JavaServiceObjectType ownerDaoType = (JavaServiceObjectType)ctx.getType(ownerName+"Dao");
 				JavaServiceObjectType ownedDaoType = (JavaServiceObjectType)ctx.getType(ownedName+"Dao");
 
 				JavaBeanType ownerType = (JavaBeanType)ctx.getType(ownerName);
 				JavaBeanType ownedType = (JavaBeanType)ctx.getType(ownedName);
 
 				if (ownerType==null) {
 					throw new JavascribeException("Invalid owner entity in relationship '"+r+"'");
 				}
 				if (ownedType==null) {
 					throw new JavascribeException("Invalid owned entity in relationship '"+r+"'");
 				}
 
 				Java5SourceFile ownerDaoSource = JsomUtils.getJavaFile(ownerDaoType.getImport(), ctx);
 				Java5SourceFile ownedDaoSource = JsomUtils.getJavaFile(ownedDaoType.getImport(), ctx);
 
 				String ownerIdField = JavascribeUtils.getLowerCamelName(ownerName)+"Id";
 				String ownedIdField = JavascribeUtils.getLowerCamelName(ownedName)+"Id";
 
 				if (relType==1) {
 					System.out.println("Processing ONE TO ONE relationship between "+ownerName+" and "+ownedName);
 					// Owner type must have a FK to the owned type
 					if (ownerType.getAttributeType(ownedIdField)==null) {
 						throw new JavascribeException("Error: In a 1-to-1 relationship, the owning entity must have a FK to the aggregated entity");
 					}
 					
 					// Create "Owned OwnedDao.getOwned(ownerId)"
 					Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 					method.setMethodName("get"+ownedName+"For"+ownerName);
 					method.addArg("integer", ownerIdField);
 					method.setReturnType(ownedName);
 					Java5CodeSnippet code = new Java5CodeSnippet();
 					method.setMethodBody(code);
 					ownedDaoSource.getPublicClass().addMethod(method);
 					ownedDaoType.addMethod(JsomUtils.createJavaOperation(method));
 
 					code.addImport("javax.persistence.Query");
 					code.append("Query _query = entityManager.createQuery(\"select _owned from ")
 							.append(ownerName+" as _owner, "+ownedName+" as _owned where _owner.")
 							.append(ownedIdField+" = _owned."+ownedIdField+" and owner."+ownerIdField)
 							.append(" = :ownerIdField\");\n");
 					code.append("_query.setParameter(\"ownerIdField\","+ownerIdField+");\n");
 					code.append(ownedName+" _ret = null;\n");
 					code.append("try {\n");
 					code.append("_ret = ("+ownedName+")_query.getSingleResult();\n");
 					code.addImport(ownedType.getImport());
 					code.append("} catch(javax.persistence.NoResultException e) { }\n");
 					code.append("return _ret;");
 					
 					// Create "Owner OwnerDao.getOwner(ownenId)"
 					method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 					method.setMethodName("get"+ownerName+"For"+ownedName);
 					method.addArg("integer", ownedIdField);
 					method.setReturnType(ownerName);
 					code = new Java5CodeSnippet();
 					method.setMethodBody(code);
 					ownerDaoSource.getPublicClass().addMethod(method);
 					ownerDaoType.addMethod(JsomUtils.createJavaOperation(method));
 
 					code.addImport("javax.persistence.Query");
 					code.append("Query _query = entityManager.createQuery(\"from "+ownerName)
 							.append(" _owner where _owner."+ownedIdField+" = :ownedIdField\");\n");
 					code.append("_query.setParameter(\"ownedIdField\","+ownedIdField+");\n");
 					code.append(ownerName+" _ret = null;\n");
 					code.append("try {\n");
 					code.append("_ret = ("+ownerName+")_query.getSingleResult();\n");
 					code.addImport(ownerType.getImport());
 					code.append("} catch(javax.persistence.NoResultException e) { }\n");
 					code.append("return _ret;");
 					
 				} else {
 					System.out.println("Processing ONE TO MANY relationship between "+ownerName+" and "+ownedName);
 					// Owned type must have a FK to the owner type
 					if (ownedType.getAttributeType(ownerIdField)==null) {
						throw new JavascribeException("Error: In a 1-to-many relationship, the type "+ownedType.getName()+" should have a foreign key "+ownerIdField+" to entity "+ownerType.getName());
 					}
 
 					String ownedPluralName = "";
 					if (ownedName.endsWith("y")) {
 						ownedPluralName = ownedName.substring(0, ownedName.length()-2)+"ies";
 					}
 					else ownedPluralName = ownedName+'s';
 
 					// Create "List<Owned> ownedDao.getOwned(ownerId)"
 					Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 					method.setMethodName("get"+ownedPluralName+"For"+ownerName);
 					method.setReturnType("list/"+ownedType.getName());
 					ownerDaoSource.addImport(ownedType.getImport());
 					method.addArg("integer", ownerIdField);
 					Java5CodeSnippet code = new Java5CodeSnippet();
 					method.setMethodBody(code);
 					ownedDaoType.addMethod(JsomUtils.createJavaOperation(method));
 					ownedDaoSource.getPublicClass().addMethod(method);
 					code.addImport("javax.persistence.Query");
 					code.append("Query _query = entityManager.createQuery(\"select _owned from ")
 							.append(ownedName+" as _owned where _owned."+ownerIdField)
 							.append(" = :ownerIdField");
 /*
 					.append(ownerName+" as _owner, "+ownedName+" as _owned where _owner.")
 							.append(ownerIdField+" = _owned."+ownerIdField+" and _owner."+ownerIdField)
 							.append(" = :ownerIdField\");\n");
 					*/
 					if (orderByPk) {
 						code.append(" order by "+ownedIdField);
 					}
 					code.append("\");\n");
 					code.append("_query.setParameter(\"ownerIdField\","+ownerIdField+");\n");
 					code.addImport("java.util.List");
 					code.addImport(ownedType.getImport());
 					code.append("List<"+ownedName+"> _ret = _query.getResultList();\n");
 					code.append("return _ret;");
 
 					// Create "Owner ownerDao.getOwner(ownedId)"
 					method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 					method.setMethodName("get"+ownerName+"For"+ownedName);
 					method.setReturnType(ownerName);
 					method.addArg("integer", ownedIdField);
 					code = new Java5CodeSnippet();
 					method.setMethodBody(code);
 					ownerDaoSource.getPublicClass().addMethod(method);
 					ownerDaoType.addMethod(JsomUtils.createJavaOperation(method));
 					code.addImport("javax.persistence.Query");
 					code.append("Query _query = entityManager.createQuery(\"select _owner from ")
 						.append(ownerName+" as _owner, "+ownedName+" as _owned where _owner.")
 						.append(ownerIdField+" = _owned."+ownerIdField+" and _owned."+ownedIdField)
 						.append(" = :ownedIdField\");\n");
 					code.append("_query.setParameter(\"ownedIdField\","+ownedIdField+");\n");
 
 					code.append(ownerName+" _ret = null;\n");
 					code.append("try {\n");
 					code.addImport(ownerType.getImport());
 					code.append("_ret = ("+ownerName+")_query.getSingleResult();\n");
 					code.append("} catch(javax.persistence.NoResultException e) { }\n");
 					code.append("return _ret;");
 				}
 
 			} catch(CodeGenerationException e) {
 				throw new JavascribeException("JSOM Exception while processing entity relationships",e);
 			}
 		}
 	}
 
 }
