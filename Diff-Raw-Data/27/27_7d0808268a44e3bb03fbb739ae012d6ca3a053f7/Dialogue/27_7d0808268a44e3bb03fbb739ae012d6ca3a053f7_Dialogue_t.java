 /*
  *  Copyright (C) 2011 The Animo Project
  *  http://animotron.org
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 3
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 package org.animotron.animi;
 
 import javolution.util.FastList;
import org.animotron.expression.AbstractExpression;
 import org.animotron.expression.AnimoExpression;
 import org.animotron.graph.AnimoGraph;
import org.animotron.graph.builder.FastGraphBuilder;
 import org.animotron.graph.serializer.CachedSerializer;
 import org.animotron.io.PipedOutput;
 import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.REF;
 import org.animotron.statement.query.GET;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.IndexHits;
 
 import java.io.*;
 import java.util.List;
 import java.util.UUID;
 
 import static org.animotron.animi.Words.NAME;
 import static org.animotron.animi.Words.words;
 import static org.animotron.expression.JExpression._;
 
 /**
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  *
  */
 public class Dialogue implements Runnable {
 	
 	//private InputStream in;
 	private OutputStream out;
 	
     private Reader reader;
     //private Writer writer;
     
     private PipedOutput<Relationship> leaned;
 
     public Dialogue(Reader in, PipedOutput<Relationship> leaned) {
     	//this.in = null;
     	this.out = null;
     	
     	reader = in;
     	//writer = null;
 
     	this.leaned = leaned;
     }
 
     public Dialogue(Reader in, OutputStream out) {
     	//this.in = null;
     	this.out = out;
     	
     	reader = in;
     	//writer = null;
 
     	this.leaned = null;
     }
 
     public Dialogue(InputStream in, OutputStream out) {
     	//this.in = in;
     	this.out = out;
     	
     	reader = new InputStreamReader(in);
     	//writer = new PrintWriter(out);
     }
     
     private StringBuilder s = new StringBuilder();
 
     public void run() {
         int len;
         char[] buff = new char[1024];
         
         String word = null;
         
         List<Relationship> token = null;
 
         try {
 			while ((len=reader.read(buff))>0) {
 			    for (int i = 0; i < len; i++) {
 			        char ch = buff[i];
 			        
 			        if (ch == '\n') {
 			        	if (token == null || token.size() == 0) {
 			        		//learn
 			        		
 							word = s.toString();
 			        		if (word != null) {
 				        		AnimoExpression expr;
 				        		
 				        		Transaction tx = AnimoGraph.beginTx();
 				        		try {
 				            		expr = new AnimoExpression("the "+uuid()+" have name \""+s.toString()+"\".");
 				            		
 				            		words().add(expr, word);
 				            		
 				            		tx.success();
 				        		} catch (Exception e) {
 									//reset
 									s = new StringBuilder();
 									continue;
 				        		} finally {
 				        			AnimoGraph.finishTx(tx);
 				        			word = null;
 				        		}
 				        		if (leaned != null) leaned.write(expr);
 				        		
 				        		if (token == null)
 				        			token = new FastList<Relationship>();
 				        		
 				        		token.add(expr);
 			        		}
 			        	} else {
 			        		if (leaned != null) {
 			        			for (Relationship r : token) {
 			        				leaned.write(r);
 			        			}
 			        		}
 			        	}
 			        	
 		        		if (out != null)  {
 		        			
 		        			try {
		        				final Object[] ans = new Object[token.size()];
 		        				int j = 0;
 		        				for (Relationship t : token) {
 		        					ans[j++] = _(AN._, t.getEndNode());
 		        				}
 			        			CachedSerializer.STRING.serialize(
                                        new AbstractExpression(new FastGraphBuilder()) {
                                            @Override
                                            public void build() throws Exception {
                                                builder.start(GET._);
                                                builder._(REF._, NAME);
                                                builder.start(AN._);
                                                for (Object o : ans){
                                                    builder._(REF._, o);
                                                }
                                                builder.end();
                                                builder.end();
                                            }
                                        },
                                         out
                                 );
 		        			} catch (Exception e) {
 		        				//XXX: log?
 							}
 		        		}
 			        } else {
 			        
 			            s.append(ch);
 						word = s.toString();
 	
 				        token = check(word);
 				        if (token != null && token.size() > 0) {
 				        	s = new StringBuilder();
 				        }
 			        }
 			    }
 
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (leaned != null)
 				try {
 					leaned.close();
 				} catch (IOException e) {}
 			if (out != null)
 				try {
 					out.close();
 				} catch (IOException e) {}
 		}
     	
     }
 
 	private List<Relationship> check(String word) {
 		
 		List<Relationship> nodes = null;
 		
 		IndexHits<Relationship> hits = words().search(word);
 		try {
 			nodes = new FastList<Relationship>(hits.size());
 			while (hits.hasNext()) {
 				nodes.add(hits.next());
 			}
 		} finally {
 			hits.close();
 		}
 		
 		return nodes;
 	}
 	
 	private String uuid() {
 		return UUID.randomUUID().toString();
 	}
 }
