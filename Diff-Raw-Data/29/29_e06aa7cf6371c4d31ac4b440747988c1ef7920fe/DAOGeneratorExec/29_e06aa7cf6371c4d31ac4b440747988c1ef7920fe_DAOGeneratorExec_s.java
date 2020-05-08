 package com.digitolio.jdbi.codegen.codemodel;
 
 import com.digitolio.jdbi.annotations.CodeGen;
 import com.digitolio.jdbi.codegen.Scanner;
 import com.digitolio.jdbi.strategy.SnakeCaseTranslatingStrategy;
 import com.digitolio.jdbi.table.Table;
 import com.digitolio.jdbi.table.TableResolver;
 import com.sun.codemodel.JClassAlreadyExistsException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 /**
  * @author C.Koc
  */
 public class DAOGeneratorExec {
 
     public static void main(String[] args) {
 
        if(args[0].contains("OVERWRITE_THIS_VALUE")){return;}
 
         SnakeCaseTranslatingStrategy strategy = new SnakeCaseTranslatingStrategy();
         TableResolver tableResolver = new TableResolver();
         Scanner scanner = new Scanner();
         Set<Class<?>> classes = scanner.scanPackage(args[0], CodeGen.CODE_GEN);
         File targetDir = new File(args[1]);
 
         for (Class<?> aClass : classes) {
             Table resolve = tableResolver.resolve(aClass, strategy);
             DAOGenerator daoGenerator = new DAOGenerator(aClass, resolve, targetDir);
             try {
                 daoGenerator.generate();
             } catch (JClassAlreadyExistsException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
