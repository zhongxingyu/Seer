 /*
  * SonarQube PHP Plugin
  * Copyright (C) 2010 SonarSource and Akram Ben Aissi
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.php.checks;
 
 import com.sonar.sslr.squid.checks.CheckMessagesVerifier;
 import org.junit.Test;
 import org.sonar.php.PHPAstScanner;
 import org.sonar.plugins.php.CheckTest;
 import org.sonar.plugins.php.TestUtils;
 import org.sonar.squid.api.SourceFile;
 
 public class TodoTagPresenceCheckTest extends CheckTest {
 
   @Test
   public void test() {
     SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("TodoTagPresenceCheck.php"), new TodoTagPresenceCheck());
     CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(4).withMessage("Complete the task associated to this TODO comment.")
       .next().atLine(8)
       .next().atLine(9)
       .next().atLine(12)
       .next().atLine(14)
       .next().atLine(16);
   }
 }
