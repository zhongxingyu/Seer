 package de.ismll.table.impl;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.TreeSet;
 
 import de.ismll.bootstrap.CommandLineParser;
 import de.ismll.table.Matrices;
 import de.ismll.table.Matrix;
 import de.ismll.table.MatrixTest;
 import de.ismll.table.ReaderConfig;
 import de.ismll.table.io.weka.ArffDataset;
 
 public class DefaultMatrixTest extends MatrixTest{
 
 	//	@Test
 	public void testRead2() throws IOException {
 		DefaultMatrix read = DefaultMatrix.read(new File("h:/test.file"));
 	}
 
 	//	@Test
 	public void testRead() throws IOException {
 		File f = File.createTempFile("matrix", "test");
 		DefaultMatrix createSparseMatrix = createSparseMatrix(100, 3000, 0.1f);
 		Matrices.write(createSparseMatrix, f);
 		DefaultMatrix.debug=true;
 		Matrices.debug=true;
 		System.out.println("ok...");
 		DefaultMatrix ok = DefaultMatrix.readWithReader(new BufferedReader(new FileReader(f)), 100000);
 		System.out.println("check ...");
 		DefaultMatrix check = DefaultMatrix.read(f, 100000);
//		FileInputStream fis = new FileInputStream(f);
 		System.out.println("check2 ...");
 		//		DefaultMatrix check2 = DefaultMatrix.readWithFileChannel(fis.getChannel(), 100000);
 
 		checkSame(ok, check);
 		//		checkSame(ok, check2);
 
 	}
 
 
 
 	//	@Test
 	public void testSpeed() throws IOException {
 		File f = File.createTempFile("matrix", "speed");
 		DefaultMatrix createSparseMatrix = createSparseMatrix(100, 1000, 0.1f);
 		Matrices.write(createSparseMatrix, f);
 		DefaultMatrix.debug=true;
 		Matrices.debug=true;
 		DefaultMatrix.read(  f, 1000000);
 		DefaultMatrix.read(  f, 1000000);
 		DefaultMatrix.readDirect(f.getAbsolutePath(), 1000000);
 
 	}
 
 
 	//	@Test
 	public void testAutodetectFormatBlankLines() throws IOException {
 		String file = "1,2,3\n1,3,2\n4,3,2\n1,1,1\n9,3,2\n\n\n";
 		DefaultMatrix.debug=true;
 		Matrices.debug=true;
 		ReaderConfig rc = new ReaderConfig();
 		rc.skipLines=0;
 		rc.autodetectFormat=true;
 
 		DefaultMatrix.DefaultMatrixParser p = new DefaultMatrix.DefaultMatrixParser();
 
 		ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
 
 		Matrices.readDense( bis, rc, p);
 		System.out.println(p.m);
 
 		rc.skipLines=1;
 		rc.autodetectFormat=true;
 
 		p = new DefaultMatrix.DefaultMatrixParser();
 
 		bis = new ByteArrayInputStream(file.getBytes());
 
 		Matrices.readDense( bis, rc, p);
 		System.out.println(p.m);
 
 
 	}
 
 
 	//	@Test
 	public void testAutodetectFormat() throws IOException {
 		String file = "1,2,3\n1,3,2\n4,3,2\n1,1,1\n9,3,2";
 		DefaultMatrix.debug=true;
 		Matrices.debug=true;
 		ReaderConfig rc = new ReaderConfig();
 		rc.skipLines=0;
 		rc.autodetectFormat=true;
 
 		DefaultMatrix.DefaultMatrixParser p = new DefaultMatrix.DefaultMatrixParser();
 
 		ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
 
 		Matrices.readDense( bis, rc, p);
 		System.out.println(p.m);
 
 		rc.skipLines=1;
 		rc.autodetectFormat=true;
 
 		p = new DefaultMatrix.DefaultMatrixParser();
 
 		bis = new ByteArrayInputStream(file.getBytes());
 
 		Matrices.readDense( bis, rc, p);
 		System.out.println(p.m);
 
 
 	}
 
 	//	@Test
 	public void createDataFile() {
 		DefaultMatrix m = new DefaultMatrix(10, 4);
 
 		int currentStudent=0;
 		TreeSet<Integer> problemHierarchyBlacklist = new TreeSet<Integer>();
 
 		int[] problemHierarchies = new int[2];
 		for (int i = 0; i < problemHierarchies.length; i++)
 			problemHierarchies[i] = i;
 
 		TreeSet<Integer> problemNameBlacklist = new TreeSet<Integer>();
 
 		int[] problemName = new int[3];
 		for (int i = 0; i < problemName.length; i++)
 			problemName[i] = i;
 
 		int numIter = 0;
 		//		int maxIter = problemHierarchies.length/2;
 		int maxIter = 2;
 
 		for (int i = 0; i < m.getNumRows(); i++) {
 			// rowID
 			//			System.out.println("row " + i + " (Student " + currentStudent + ")");
 			if (i>0&&i%5000==0)
 				System.out.println("Row " + i);
 			m.set(i, 0, i);
 			m.set(i, 1, currentStudent);
 			if (Math.random()<0.007) {
 				System.out.println("Student " + currentStudent);
 				currentStudent++;
 				problemHierarchyBlacklist.clear();
 				problemNameBlacklist.clear();
 			}
 
 			// problem hierarchy
 			numIter=0;
 
 			int idx=-1;
 			while (idx < 0 && numIter < maxIter) {
 				int cu =  Math.min((int)(Math.random()*(problemHierarchies.length+1)), problemHierarchies.length-1);
 				Integer v = Integer.valueOf(cu);
 				if (!problemHierarchyBlacklist.contains(v))
 					idx = cu;
 				numIter++;
 			}
 			//			System.out.println("1:" + numIter);
 			if (numIter>maxIter || idx < 0) {
 				i--;
 				continue;
 			}
 			problemHierarchyBlacklist.add(Integer.valueOf(idx));
 
 			m.set(i, 2, problemHierarchies[idx]);
 
 			// problem name
 
 			numIter=0;
 
 			idx=-1;
 			while (idx < 0&& numIter < maxIter) {
 				int cu =  Math.min((int)(Math.random()*(problemName.length+1)), problemName.length-1);
 				Integer v = Integer.valueOf(cu);
 				if (!problemNameBlacklist.contains(v))
 					idx = cu;
 				numIter++;
 			}
 			//			System.out.println("2:" + numIter);
 			if (numIter>maxIter || idx < 0) {
 				i--;
 				continue;
 			}
 			problemNameBlacklist.add(Integer.valueOf(idx));
 
 			m.set(i, 3, problemName[idx]);
 
 		}
 
 		File f = new File("test2.txt");
 		try {
 			m.write(f.getAbsolutePath(), 1000000);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	//	@Test
 	public void testBinary0() throws IOException {
 		DefaultMatrix m = new DefaultMatrix(1000, 100);
 		Matrices.debug=true;
 		Matrices.fillUniformAtRandom(m, 0, 10.0f);
 
 		File csvFormat = File.createTempFile("matrices", "csv");
 		File binaryFormat = File.createTempFile("matrices", "binary");
 		csvFormat.deleteOnExit();
 		binaryFormat.deleteOnExit();
 
 		Matrices.write(m, csvFormat);
 		Matrices.writeBinary(m, binaryFormat, (byte)0x00);
 
 		System.out.println("CSV size: " + csvFormat.length());
 		System.out.println("bin size: " + binaryFormat.length());
 
 		DefaultMatrix readCsv = DefaultMatrix.read(csvFormat, 10000);
 		Matrix readBinary = Matrices.readBinary(binaryFormat);
 
 		checkSame(m, readCsv);
 		checkSame(m, readBinary);
 
 	}
 
 	//	@Test
 	public void testBinary1() throws IOException {
 		DefaultMatrix m = new DefaultMatrix(100, 100);
 		Matrices.debug=true;
 		Matrices.fillUniformAtRandom(m, 0, 10.0f);
 
 		File csvFormat = File.createTempFile("matrices", "csv");
 		File binaryFormat = File.createTempFile("matrices", "binary");
 
 		//		csvFormat.deleteOnExit();
 		//		binaryFormat.deleteOnExit();
 
 
 		Matrices.write(m, csvFormat);
 		Matrices.writeBinary(m, binaryFormat, (byte)0x01);
 
 		System.out.println("CSV size: " + csvFormat.length());
 		System.out.println("bin size: " + binaryFormat.length());
 
 		DefaultMatrix readCsv = DefaultMatrix.read(csvFormat, 10000);
 		Matrix readBinary = Matrices.readBinary(binaryFormat);
 
 		checkSame(m, readCsv);
 		checkSame(m, readBinary);
 
 	}
 
 	//	@Test
 	public void bootstrapTestLoadV04() throws IOException {
 		File f = File.createTempFile("matrix", "loadTest");
 		DefaultMatrix createSparseMatrix = createSparseMatrix(100, 10, 0f);
 		Matrices.write(createSparseMatrix, f);
 
 		BootstrapLoadTest t = new BootstrapLoadTest();
 		CommandLineParser.parseCommandLine(new String[] {
 				"m=" + f.getAbsolutePath()
 		}, t);
 		super.checkSame(createSparseMatrix, t.getM());
 	}
 
 }
