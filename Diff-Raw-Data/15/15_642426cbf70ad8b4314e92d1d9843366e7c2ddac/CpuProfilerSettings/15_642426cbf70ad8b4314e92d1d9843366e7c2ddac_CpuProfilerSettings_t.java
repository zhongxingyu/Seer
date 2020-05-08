 /*
  * This file is part of VisualVM for IDEA
  *
  * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice,
  *       this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above copyright notice,
  *       this list of conditions and the following disclaimer in the documentation
  *       and/or other materials provided with the distribution.
  *
  *     * Neither the name of the copyright holder nor the names of its contributors
  *       may be used to endorse or promote products derived from this software
  *       without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.orfjackal.visualvm4idea.plugin;
 
 import com.intellij.execution.configurations.JavaParameters;
import com.intellij.openapi.util.*;
 import com.intellij.util.xmlb.XmlSerializer;
 import net.orfjackal.visualvm4idea.visualvm.CpuSettings;
 import org.jdom.Element;
import org.jetbrains.annotations.*;
 
 /**
  * @author Esko Luontola
  * @since 30.10.2008
  */
 public class CpuProfilerSettings implements ProfilerSettings {
 
     @NotNull public StartFrom startFromMode = StartFrom.MAIN_CLASS;
     @NotNull private String mainClassToStartFrom = "";
     @NotNull public String otherClassesToStartFrom = "";
 
     public boolean profileNewRunnables = true;
 
     @NotNull public FilterMode filteringMode = FilterMode.EXCLUDE;
     @NotNull public String filterIncludeClasses = "";
     @NotNull public String filterExcludeClasses = CpuSettings.DEFAULT_EXCLUDES;
 
     private int appUniqueId;
 
     public void readExternal(Element element) throws InvalidDataException {
         XmlSerializer.deserializeInto(this, element);
     }
 
     public void writeExternal(Element element) throws WriteExternalException {
         element.setContent(XmlSerializer.serialize(this).removeContent());
     }
 
     public void configureOnPatch(JavaParameters javaParameters) {
        mainClassToStartFrom = nonnull(javaParameters.getMainClass());
         appUniqueId = IdGenerator.nextId();
     }
 
    @NotNull
    private static String nonnull(@Nullable String s) {
        return s != null ? s : "";
    }

     public int getAppUniqueId() {
         return appUniqueId;
     }
 
     public String getClassesToProfileFrom() {
         if (startFromMode == StartFrom.MAIN_CLASS) {
             return mainClassToStartFrom;
         } else {
             return otherClassesToStartFrom;
         }
     }
 
     public CpuSettings.FilterType getFilterType() {
         if (filteringMode == FilterMode.INCLUDE) {
             return CpuSettings.FilterType.INCLUDE;
         } else {
             return CpuSettings.FilterType.EXCLUDE;
         }
     }
 
     public String getFilterValue() {
         if (filteringMode == FilterMode.INCLUDE) {
             return filterIncludeClasses;
         } else {
             return filterExcludeClasses;
         }
     }
 
     public enum StartFrom {
         MAIN_CLASS, OTHER_CLASSES
     }
 
     public enum FilterMode {
         INCLUDE, EXCLUDE
     }
 }
