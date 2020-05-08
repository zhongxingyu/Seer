 package org.consulo.google.protobuf.java;
 
 import org.consulo.google.protobuf.module.extension.GoogleProtobufModuleExtensionProvider;
 import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
 
 /**
  * @author VISTALL
  * @since 06.07.13.
  */
 public class GoogleProtobufJavaModuleExtensionProvider extends GoogleProtobufModuleExtensionProvider<GoogleProtobufJavaModuleExtension, GoogleProtobufJavaMutableModuleExtension> {
   @NotNull
   @Override
   public GoogleProtobufJavaModuleExtension createImmutable(@NotNull String s, @NotNull Module module) {
     return new GoogleProtobufJavaModuleExtension(s, module);
   }
 
   @NotNull
   @Override
   public GoogleProtobufJavaMutableModuleExtension createMutable(@NotNull String s, @NotNull Module module, @NotNull GoogleProtobufJavaModuleExtension googleProtobufJavaModuleExtension) {
     return new GoogleProtobufJavaMutableModuleExtension(s, module,  googleProtobufJavaModuleExtension);
   }
 }
