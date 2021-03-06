 /*
  * Copyright 2000-2011 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jetbrains.buildServer.nuget.tests.integration;
 
 import jetbrains.buildServer.util.FileUtil;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.File;
 
 /**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 31.08.11 10:39
 */
 public enum NuGet {
   NuGet_1_4,
   NuGet_1_5,
   ;
 
   @NotNull
   public File getPath() {
     switch (this) {
       case NuGet_1_4:
         return FileUtil.getCanonicalFile(new File("./nuget-tests/testData/nuget/1.4/NuGet.exe"));
       case NuGet_1_5:
        return FileUtil.getCanonicalFile(new File("./nuget-tests/testData/nuget/1.4/NuGet.exe"));
       default:
         throw new IllegalArgumentException("Failed to find nuget " + this);
     }
   }
 }
