 /*
  * Copyright 2011 Samuel B. Johnson
  * This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package net.samuelbjohnson.javadev.crosstopix;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.commons.lang3.StringUtils;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandler;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.turtle.TurtleWriter;
 import org.openrdf.sail.memory.MemoryStore;
 import org.openrdf.sail.memory.model.MemURI;
 
 public class Joiner {
 	
 	private Repository imslpRep;
 	private RepositoryConnection imslpCon;
 	
 	private Repository cpdlRep;
 	private RepositoryConnection cpdlCon;
 	
 	private PrintWriter outputWriter;
 	private Repository outputRep;
 	private RepositoryConnection outputRepCon;
 	
 	private String predicateNamespace;
 	private String subjectNamespace;
 	
 	private ValueFactory valueFactory;
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws RDFHandlerException 
 	 * @throws RDFParseException 
 	 * @throws NoSuchAlgorithmException 
 	 */
 	public static void main(String[] args) throws RDFParseException, RDFHandlerException, IOException, RepositoryException, NoSuchAlgorithmException {
 		String file1, file2;
 		
 		if (args.length < 1) {
 			System.err.println("No input files specified");
 			return;
 		} else if (args.length == 1) {
 			file1 = args[0];
 			file2 = args[0];
 		} else {
 			file1 = args[0];
 			file2 = args[1];
 		}
 		
 		Joiner j = new Joiner(file1, file2);
 		
 		if(args.length == 3) {
 			if (args[2].equals("-numbersOnly")) {
 				j.processJoin(false);
 				return;
 			}
 			if (args[2].equals("-normalizeForLength")) {
 				j = new LengthNormalizedJoiner(file1, file2);
 			}
 		}
 		
 		
 		j.processJoin(true);
 	}
 	
 	public Joiner(String imslpFile, String cpdlFile) throws RepositoryException, RDFParseException, IOException, NoSuchAlgorithmException {		
 		outputWriter = new PrintWriter(new OutputStreamWriter(System.out));
 		
 		outputRep = new SailRepository(new MemoryStore());
 		outputRep.initialize();
 		outputRepCon = outputRep.getConnection();
 		
 		predicateNamespace = "http://purl.org/twc/vocab/cross-topix#";
 		subjectNamespace = "http://beta.twc.rpi.edu/id/cross-topix/alpha/";
 		
 		valueFactory = ValueFactoryImpl.getInstance();
 		
 		imslpRep = new SailRepository(new MemoryStore());
 		imslpRep.initialize();
 		imslpCon = imslpRep.getConnection();
 		
 		cpdlRep = new SailRepository(new MemoryStore());
 		cpdlRep.initialize();
 		cpdlCon = cpdlRep.getConnection();
 		
 		imslpCon.add(new File(imslpFile), "", RDFFormat.TURTLE, new Resource[0]);
 		cpdlCon.add(new File(cpdlFile), "", RDFFormat.TURTLE, new Resource[0]);
 	}
 
 	public void processJoin(boolean fullOutput) throws RepositoryException, NoSuchAlgorithmException, RDFHandlerException {
 		
 		RepositoryResult<Statement> imslpResults= imslpCon.getStatements(null, null, null, false, new Resource[0]);
 		int counter = 1;
 		RDFHandler handler = new TurtleWriter(outputWriter);
 		while(imslpResults.hasNext()) {
 			counter ++;
 			Statement imslpStatement = imslpResults.next();
 			RepositoryResult<Statement> cpdlResults = cpdlCon.getStatements(null, null, null, false, new Resource[0]);
 			while(cpdlResults.hasNext()) {
 				Statement cpdlStatement = cpdlResults.next();
 				join(imslpStatement, cpdlStatement, fullOutput);
 				
 				if (fullOutput) {
 					if (outputRepCon.size(new Resource[0]) >= 50000) {
 						outputRepCon.export(handler, new Resource[0]);
 						outputRepCon.clear(new Resource[0]);
 						outputRepCon.commit();
 					}
 				}
 			}
 		}
 		if (fullOutput) {
 			outputRepCon.export(handler);
 		}
 		
 		outputWriter.flush();
 		outputWriter.close();
 	}
 	
 	public boolean join(Statement imslpStatement, Statement cpdlStatement, boolean fullOutput) throws NoSuchAlgorithmException, RepositoryException {
 		BigDecimal distance = computeDistance(imslpStatement.getObject().stringValue(), cpdlStatement.getObject().stringValue());
 		
 		if (!fullOutput) {
 			outputWriter.print(imslpStatement.getObject().stringValue().length() + ",");
 			outputWriter.print(cpdlStatement.getObject().stringValue().length() + ",");
 			outputWriter.println(distance);
 			return true;
 		}
 		
 		MessageDigest md5 = MessageDigest.getInstance("MD5");
 		md5.update((
 				imslpStatement.getObject().stringValue() +
 				cpdlStatement.getObject().stringValue() +
 				distance.toPlainString()
 		).getBytes());
 		BigInteger hashInt = new BigInteger(1, md5.digest());
 		String md5Sum = hashInt.toString(16);
 		
 		Resource subject = valueFactory.createURI(subjectNamespace, md5Sum);
 		URI predicateImslp = valueFactory.createURI(predicateNamespace, "comparable_1");
 		URI predicateCpdl = valueFactory.createURI(predicateNamespace, "comparable_2");
 		URI predicateDiff = valueFactory.createURI(predicateNamespace, "similarity");
 		
 		outputRepCon.add(subject, predicateImslp, imslpStatement.getSubject());
 		outputRepCon.add(subject, predicateCpdl, cpdlStatement.getSubject());
		outputRepCon.add(subject, predicateDiff, valueFactory.createLiteral(distance.doubleValue()));
 		
 		outputRepCon.commit();
 		
 		return true;
 	}
 	
 	protected BigDecimal computeDistance(String string1, String string2) {
 		return new BigDecimal(StringUtils.getLevenshteinDistance(string1, string2));
 	}
 
 }
