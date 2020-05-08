 package org.consulo.google.protobuf.java;
 
import com.intellij.openapi.module.Module;
 import org.consulo.google.protobuf.module.extension.GoogleProtobufModuleExtensionProvider;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * @author VISTALL
  * @since 06.07.13.
  */
 public class GoogleProtobufJavaModuleExtensionProvider extends GoogleProtobufModuleExtensionProvider<GoogleProtobufJavaModuleExtension, GoogleProtobufJavaMutableModuleExtension> {
   @NotNull
   @Override
  public Class<GoogleProtobufJavaModuleExtension> getImmutableClass() {
    return GoogleProtobufJavaModuleExtension.class;
  }

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
