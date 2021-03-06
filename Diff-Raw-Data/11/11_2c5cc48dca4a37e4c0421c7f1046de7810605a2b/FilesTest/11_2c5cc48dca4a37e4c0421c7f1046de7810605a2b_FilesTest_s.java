 /*
  * OpenSpotLight - Open Source IT Governance Platform
  *  
  * Copyright (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA 
  * or third-party contributors as indicated by the @author tags or express 
  * copyright attribution statements applied by the authors.  All third-party 
  * contributions are distributed under license by CARAVELATECH CONSULTORIA E 
  * TECNOLOGIA EM INFORMATICA LTDA. 
  * 
  * This copyrighted material is made available to anyone wishing to use, modify, 
  * copy, or redistribute it subject to the terms and conditions of the GNU 
  * Lesser General Public License, as published by the Free Software Foundation. 
  * 
  * This program is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * 
  * See the GNU Lesser General Public License  for more details. 
  * 
  * You should have received a copy of the GNU Lesser General Public License 
  * along with this distribution; if not, write to: 
  * Free Software Foundation, Inc. 
  * 51 Franklin Street, Fifth Floor 
  * Boston, MA  02110-1301  USA 
  * 
  *********************************************************************** 
  * OpenSpotLight - Plataforma de Governana de TI de Cdigo Aberto 
  *
  * Direitos Autorais Reservados (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA 
  * EM INFORMATICA LTDA ou como contribuidores terceiros indicados pela etiqueta 
  * @author ou por expressa atribuio de direito autoral declarada e atribuda pelo autor.
  * Todas as contribuies de terceiros esto distribudas sob licena da
  * CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA. 
  * 
  * Este programa  software livre; voc pode redistribu-lo e/ou modific-lo sob os 
  * termos da Licena Pblica Geral Menor do GNU conforme publicada pela Free Software 
  * Foundation. 
  * 
  * Este programa  distribudo na expectativa de que seja til, porm, SEM NENHUMA 
  * GARANTIA; nem mesmo a garantia implcita de COMERCIABILIDADE OU ADEQUAO A UMA
  * FINALIDADE ESPECFICA. Consulte a Licena Pblica Geral Menor do GNU para mais detalhes.  
  * 
  * Voc deve ter recebido uma cpia da Licena Pblica Geral Menor do GNU junto com este
  * programa; se no, escreva para: 
  * Free Software Foundation, Inc. 
  * 51 Franklin Street, Fifth Floor 
  * Boston, MA  02110-1301  USA
  */
 
 package org.openspotlight.common.util.test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.openspotlight.common.util.Files.delete;
 import static org.openspotlight.common.util.Files.listFileNamesFrom;
 import static org.openspotlight.common.util.Files.readBytesFromStream;
 import static org.openspotlight.common.util.Strings.removeBegginingFrom;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openspotlight.common.util.Files;
 
 /**
  * Test class for {@link Files}
  * 
  * @author Luiz Fernando Teston - feu.teston@caravelatech.com
  * 
  */
 @SuppressWarnings("all")
 public class FilesTest {
     
    private static String LOWEST_PATH = "./target"; //$NON-NLS-1$
     
    private static String LOWER_PATH = LOWEST_PATH + "/resources/testData"; //$NON-NLS-1$
     
    private static String TEST_DIR = LOWER_PATH + "/SomeOtherDir/anotherDir"; //$NON-NLS-1$
     
    private static String TEST_FILE = TEST_DIR + "/temp.txt"; //$NON-NLS-1$
     
     private static String RELATIVE_PATH_FILE = removeBegginingFrom(LOWEST_PATH,
             TEST_FILE);
     
     @SuppressWarnings("boxing")
     @Before
     public void createSomeTestData() throws Exception {
         final File dir = new File(TEST_DIR);
         final File file = new File(TEST_FILE);
         
         dir.mkdirs();
         file.createNewFile();
         
         assertThat(dir.exists(), is(true));
         assertThat(file.exists(), is(true));
         
     }
     
     @SuppressWarnings("boxing")
     @Test
     public void shouldDeleteValidDirs() throws Exception {
         delete(LOWER_PATH);
         assertThat(new File(LOWER_PATH).exists(), is(false));
     }
     
     @SuppressWarnings("boxing")
     @Test
     public void shouldDeleteValidFiles() throws Exception {
         delete(TEST_FILE);
         assertThat(new File(TEST_FILE).exists(), is(false));
     }
     
     @SuppressWarnings("boxing")
     @Test
     public void shouldListFileNamesInARecursiveWay() throws Exception {
         final Set<String> fileNames = listFileNamesFrom(LOWEST_PATH);
         assertThat(fileNames.contains(RELATIVE_PATH_FILE), is(true));
     }
     
     @Test(expected = IllegalStateException.class)
     public void shouldThrowExceptionWhenGettingInvalidFile() throws Exception {
         listFileNamesFrom("invalid base path"); //$NON-NLS-1$
     }
     
     @SuppressWarnings("boxing")
     @Test
     public void shouldWriteByteArrayFromStream() throws Exception {
         final byte[] initialContent = "initialContent".getBytes(); //$NON-NLS-1$
         final InputStream is = new ByteArrayInputStream(initialContent);
         final byte[] readedContent = readBytesFromStream(is);
         assertThat(Arrays.equals(initialContent, readedContent), is(true));
     }
     
 }
