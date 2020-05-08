 package unittest.parser;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import java_cup.runtime.Symbol;
 
 import org.custommonkey.xmlunit.XMLTestCase;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Node;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ErrorCollector;
 import org.xml.sax.SAXException;
 
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.parser.Lexer;
 import edu.tum.lua.parser.Parser;
 import serialization.XMLDeserializer;
 import serialization.XMLSerializer;
 
 public class TestParserXML extends XMLTestCase {
 
 	Parser parser;
 	Lexer lexer;
 	
 	String[] xmlFiles;
 	String[] luaFiles;
 	
     @Rule
     public ErrorCollector collector = new ErrorCollector();
 
 	
 	
 	@Before
 	public void setUp() {
 		List<String> xmlFiles = new ArrayList<>();
 		List<String> luaFiles = new ArrayList<>();
 		
		xmlFiles.add("testinput/grammar/for_2_exps.xml");
		luaFiles.add("testinput/grammar/for_2_exps.lua");
		
		xmlFiles.add("testinput/grammar/for.xml");
		luaFiles.add("testinput/grammar/for.lua");
		
		xmlFiles.add("testinput/grammar/explist_multiple.xml");
		luaFiles.add("testinput/grammar/explist_multiple.lua");
		
 		xmlFiles.add("testinput/grammar/func_varargs.xml");
 		luaFiles.add("testinput/grammar/func_varargs.lua");
 		
 		xmlFiles.add("testinput/grammar/funcname_colon.xml");
 		luaFiles.add("testinput/grammar/funcname_colon.lua");
 		
 		xmlFiles.add("testinput/grammar/var_prefix_dot.xml");
 		luaFiles.add("testinput/grammar/var_prefix_dot.lua");
 
 		xmlFiles.add("testinput/grammar/if_and.xml");
 		luaFiles.add("testinput/grammar/if_and.lua");
 		
 		xmlFiles.add("testinput/binop/binop_and.xml");
 		luaFiles.add("testinput/binop/binop_and.lua");
 		
 		xmlFiles.add("testinput/binop/binop_or.xml");
 		luaFiles.add("testinput/binop/binop_or.lua");
 
 		xmlFiles.add("testinput/grammar/function_def.xml");
 		luaFiles.add("testinput/grammar/function_def.lua");
 		
 		xmlFiles.add("testinput/grammar/function_def.xml");
 		luaFiles.add("testinput/grammar/function_def_whitespace_1.lua");
 		
 		xmlFiles.add("testinput/grammar/function_funcbody.xml");
 		luaFiles.add("testinput/grammar/function_funcbody.lua");
 		
 		xmlFiles.add("testinput/grammar/function_funcbody.xml");
 		luaFiles.add("testinput/grammar/function_funcbody_whitespace_1.lua");
 		
 		xmlFiles.add("testinput/grammar/function_funcbody.xml");
 		luaFiles.add("testinput/grammar/function_funcbody_whitespace_2.lua");
 		
 		xmlFiles.add("testinput/grammar/function_funcbody.xml");
 		luaFiles.add("testinput/grammar/function_funcbody_whitespace_3.lua");
 		
 		xmlFiles.add("testinput/unop/unop_length.xml");
 		luaFiles.add("testinput/unop/unop_length.lua");
 
 		xmlFiles.add("testinput/unop/unop_not.xml");
 		luaFiles.add("testinput/unop/unop_not.lua");
 
 		
 		
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_5.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_5.lua");
 		
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_1.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_1.lua");
 		
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_0.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_0.lua");
 		
 		xmlFiles.add("testinput/grammar/exp_tableconstructor_empty.xml");
 		luaFiles.add("testinput/grammar/exp_tableconstructor_empty.lua");
 		
 		xmlFiles.add("testinput/local/local_func.xml");
 		luaFiles.add("testinput/local/local_func.lua");
 		
 		xmlFiles.add("testinput/local/local_func.xml");
 		luaFiles.add("testinput/local/local_func_whitespace_1.lua");
 		
 		xmlFiles.add("testinput/local/local_noexp.xml");
 		luaFiles.add("testinput/local/local_noexp.lua");
 		
 		xmlFiles.add("testinput/local/local_multiple.xml");
 		luaFiles.add("testinput/local/local_multiple.lua");
 		
 		xmlFiles.add("testinput/local/local_single.xml");
 		luaFiles.add("testinput/local/local_single.lua");
 		
 		xmlFiles.add("testinput/grammar/table_write.xml");
 		luaFiles.add("testinput/grammar/table_write.lua");
 		
 		xmlFiles.add("testinput/binop/binop_concat.xml");
 		luaFiles.add("testinput/binop/binop_concat.lua");
 		
 		xmlFiles.add("testinput/binop/binop_div.xml");
 		luaFiles.add("testinput/binop/binop_div.lua");
 		
 		xmlFiles.add("testinput/binop/binop_eq.xml");
 		luaFiles.add("testinput/binop/binop_eq.lua");
 		
 		xmlFiles.add("testinput/binop/binop_ge.xml");
 		luaFiles.add("testinput/binop/binop_ge.lua");
 		
 		xmlFiles.add("testinput/binop/binop_gt.xml");
 		luaFiles.add("testinput/binop/binop_gt.lua");
 		
 		xmlFiles.add("testinput/binop/binop_le.xml");
 		luaFiles.add("testinput/binop/binop_le.lua");
 		
 		xmlFiles.add("testinput/binop/binop_lt.xml");
 		luaFiles.add("testinput/binop/binop_lt.lua");
 		
 		xmlFiles.add("testinput/binop/binop_mod.xml");
 		luaFiles.add("testinput/binop/binop_mod.lua");
 		
 		xmlFiles.add("testinput/binop/binop_neq.xml");
 		luaFiles.add("testinput/binop/binop_neq.lua");
 		
 		xmlFiles.add("testinput/binop/binop_plus.xml");
 		luaFiles.add("testinput/binop/binop_plus.lua");
 		
 		xmlFiles.add("testinput/binop/binop_pow.xml");
 		luaFiles.add("testinput/binop/binop_pow.lua");
 		
 		xmlFiles.add("testinput/binop/binop_sub.xml");
 		luaFiles.add("testinput/binop/binop_sub.lua");
 		
 		xmlFiles.add("testinput/binop/binop_times.xml");
 		luaFiles.add("testinput/binop/binop_times.lua");
 
 		
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_2.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_2.lua");
 
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_3.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_3.lua");
 
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_4.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_4.lua");
 
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_6.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_6.lua");
 
 		xmlFiles.add("testinput/grammar/stat_assignment_whitespace_7.xml");
 		luaFiles.add("testinput/grammar/stat_assignment_whitespace_7.lua");
 		
 		
 		xmlFiles.add("testinput/grammar/stat_assignment.xml");
 		luaFiles.add("testinput/grammar/stat_assignment.lua");
 		
 		xmlFiles.add("testinput/grammar/if_else_if.xml");
 		luaFiles.add("testinput/grammar/if_else_if.lua");
 		
 		xmlFiles.add("testinput/grammar/explist_whitespace.xml");
 		luaFiles.add("testinput/grammar/explist_whitespace.lua");
 		
 		xmlFiles.add("testinput/grammar/ifelse.xml");
 		luaFiles.add("testinput/grammar/ifelse.lua");
 		
 		xmlFiles.add("testinput/grammar/if.xml");
 		luaFiles.add("testinput/grammar/if.lua");
 		
 		xmlFiles.add("testinput/grammar/asm_whitespace_left.xml");
 		luaFiles.add("testinput/grammar/asm_whitespace_left.lua");
 		
 		xmlFiles.add("testinput/grammar/asm_whitespace_right.xml");
 		luaFiles.add("testinput/grammar/asm_whitespace_right.lua");
 		
 		xmlFiles.add("testinput/grammar/table_access.xml");
 		luaFiles.add("testinput/grammar/table_access.lua");
 		
 		xmlFiles.add("testinput/grammar/exp_nil.xml");
 		luaFiles.add("testinput/grammar/exp_nil.lua");
 		
 		xmlFiles.add("testinput/grammar/exp_prefixexp.xml");
 		luaFiles.add("testinput/grammar/exp_prefixexp.lua");
 		
 		xmlFiles.add("testinput/grammar/exp_string.xml");
 		luaFiles.add("testinput/grammar/exp_string.lua");
 		
 		xmlFiles.add("testinput/grammar/exp_unop.xml");
 		luaFiles.add("testinput/grammar/exp_unop.lua");
 		
 		xmlFiles.add("testinput/grammar/laststat_break.xml");
 		luaFiles.add("testinput/grammar/laststat_break.lua");
 		
 		xmlFiles.add("testinput/grammar/laststat_return_empty.xml");
 		luaFiles.add("testinput/grammar/laststat_return_empty.lua");
 		
 		xmlFiles.add("testinput/grammar/stat_do.xml");
 		luaFiles.add("testinput/grammar/stat_do.lua");
 		
 		xmlFiles.add("testinput/grammar/stat_foreach.xml");
 		luaFiles.add("testinput/grammar/stat_foreach.lua");
 		
 		xmlFiles.add("testinput/grammar/stat_repeat.xml");
 		luaFiles.add("testinput/grammar/stat_repeat.lua");
 		
 		xmlFiles.add("testinput/grammar/stat_while.xml");
 		luaFiles.add("testinput/grammar/stat_while.lua");
 		
 		xmlFiles.add("testinput/grammar/chunk_with_return.xml");
 		luaFiles.add("testinput/grammar/chunk_with_return.lua");
 		
 		xmlFiles.add("testinput/grammar/explist_tableconstructor.xml");
 		luaFiles.add("testinput/grammar/explist_tableconstructor.lua");
 		
 		xmlFiles.add("testinput/grammar/field_name.xml");
 		luaFiles.add("testinput/grammar/field_name.lua");
 		
 		xmlFiles.add("testinput/grammar/exp_function_application.xml");
 		luaFiles.add("testinput/grammar/exp_function_application.lua");
 		
 		xmlFiles.add("testinput/grammar/chunk_sequence_without_semicolons.xml");
 		luaFiles.add("testinput/grammar/chunk_sequence_without_semicolons.lua");
 		
 		xmlFiles.add("testinput/grammar/chunk_sequence_with_semicolons.xml");
 		luaFiles.add("testinput/grammar/chunk_sequence_with_semicolons.lua");
 		
 		xmlFiles.add("testinput/grammar/block.xml");
 		luaFiles.add("testinput/grammar/block.lua");
 		
 		xmlFiles.add("testinput/grammar/exp_tableconstructor.xml");
 		luaFiles.add("testinput/grammar/exp_tableconstructor.lua");
 
 		xmlFiles.add("testinput/grammar/args_explist_none.xml");
 		luaFiles.add("testinput/grammar/args_explist_none.lua");
 		
 		xmlFiles.add("testinput/grammar/args_explist_some.xml");
 		luaFiles.add("testinput/grammar/args_explist_some.lua");
 
 		xmlFiles.add("testinput/grammar/args_string.xml");
 		luaFiles.add("testinput/grammar/args_string.lua");
 		
 		xmlFiles.add("testinput/grammar/args_tableconstructor.xml");
 		luaFiles.add("testinput/grammar/args_tableconstructor.lua");
 		
 		this.xmlFiles = xmlFiles.toArray(new String[]{});
 		this.luaFiles = luaFiles.toArray(new String[]{});
 	}
 	
 	
 	@Test
 	public void test() throws Exception {
 		XMLDeserializer deserializer = new XMLDeserializer();
 		XMLSerializer serializer = new XMLSerializer();
         SAXReader reader = new SAXReader();
         reader.setStripWhitespaceText(true);
         reader.setMergeAdjacentText(true);
 
         Document document;
 		
 		for(int i = 0 ; i < xmlFiles.length; i++) {
 			String xmlFile = xmlFiles[i];
 			String luaFile = luaFiles[i];
 			System.out.println(luaFile);
 			// check if the file deserializes correctly
 			Block b = (Block) deserializer.deserialize(xmlFile);
 			assertNotNull("Block should not be null for file " + luaFile, b);
 
 			Lexer outputScanner = new Lexer(new java.io.FileReader(luaFile));
 
 			// print out the tokens
 			Symbol token;
 			token = outputScanner.next_token();
 			while (token.sym != 0) {
 				System.out.print(TestParser.tokenLookup(token.sym) + " ");
 				token = outputScanner.next_token();
 			}
 			System.out.println();
 			
 			
 			
 			lexer = new Lexer(new FileReader(luaFile));
 			parser = new Parser(lexer);
 			
 			Block block = (Block) parser.parse().value;
 			
 			Document doc = serializer.serialize(block);
 			doc.normalize();
 			
 			document = reader.read(xmlFile);
 			document.normalize();
 			
 			// remove Null nodes
 			List<? extends Node> nodes = document.selectNodes("//Null");
 			for(Node n : nodes) {
 				n.detach();
 			}
 
 	        OutputFormat format = OutputFormat.createPrettyPrint();
 	        XMLWriter writer = new XMLWriter( System.out, format );
 	        writer.write( doc );
 
 	        //writer.write( document);
 	        
 	        assertXMLEqual(xmlFile, doc.asXML(), document.asXML());
 
 		}
 		
 	}
 
 }
