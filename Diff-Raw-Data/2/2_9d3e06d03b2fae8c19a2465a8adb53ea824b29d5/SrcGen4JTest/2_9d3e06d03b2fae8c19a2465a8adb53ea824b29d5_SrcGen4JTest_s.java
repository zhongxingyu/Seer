 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcgen4j.commons;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 import a.b.b.Generator1;
 import a.b.b.Parser1;
 import a.b.c.Generator2;
 import a.b.c.Parser2;
 
 /**
  * Tests for {@link SrcGen4J}.
  */
 public class SrcGen4JTest {
 
     // CHECKSTYLE:OFF
 
     @Test
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public final void testExecute() throws ParseException, GenerateException {
 
         // PREPARE
         final SrcGen4JConfig config = new SrcGen4JConfig();
         final ParserConfig parserCfg1 = new ParserConfig("parser1", "a.b.b.Parser1");
         config.addParser(parserCfg1);
         final ParserConfig parserCfg2 = new ParserConfig("parser2", "a.b.c.Parser2");
         config.addParser(parserCfg2);
 
         final Generators generators = new Generators();
         config.setGenerators(generators);
 
         final GeneratorConfig genCfg1 = new GeneratorConfig("generator1");
         genCfg1.setClassName("a.b.b.Generator1");
         genCfg1.setParser("parser1");
         generators.addGenerator(genCfg1);
 
         final GeneratorConfig genCfg2 = new GeneratorConfig("generator2");
         genCfg2.setClassName("a.b.c.Generator2");
         genCfg2.setParser("parser2");
         generators.addGenerator(genCfg2);
 
         final GeneratorConfig genCfg3 = new GeneratorConfig("generator3");
         genCfg3.setClassName("a.b.c.Generator2"); // Re-use generator class
         genCfg3.setParser("parser2"); // Also connected to parser2
         generators.addGenerator(genCfg3);
 
         final SrcGen4J testee = new SrcGen4J(config);
 
         // TEST
         testee.execute();
 
         // VERIFY
 
         assertThat(parserCfg1.getParser()).isInstanceOf(Parser1.class);
         final Parser<String> p1 = (Parser) parserCfg1.getParser();
         final Parser1 parser1 = (Parser1) p1;
         assertThat(parser1.getConfig()).isSameAs(parserCfg1);
 
         assertThat(parserCfg2.getParser()).isInstanceOf(Parser2.class);
         final Parser<String> p2 = (Parser) parserCfg2.getParser();
         final Parser2 parser2 = (Parser2) p2;
         assertThat(parser2.getConfig()).isSameAs(parserCfg2);
 
         assertThat(genCfg1.getGenerator()).isInstanceOf(Generator1.class);
         final Generator<String> g1 = (Generator) genCfg1.getGenerator();
         final Generator1 generator1 = (Generator1) g1;
         assertThat(generator1.getConfig()).isSameAs(genCfg1);
         assertThat(generator1.getModel()).isEqualTo(parser1.getModel());
 
         assertThat(genCfg2.getGenerator()).isInstanceOf(Generator2.class);
         final Generator<String> g2 = (Generator) genCfg2.getGenerator();
         final Generator2 generator2 = (Generator2) g2;
         assertThat(generator2.getConfig()).isSameAs(genCfg2);
         assertThat(generator2.getModel()).isEqualTo(parser2.getModel());
 
         assertThat(genCfg3.getGenerator()).isInstanceOf(Generator2.class);
         final Generator<String> g3 = (Generator) genCfg3.getGenerator();
         final Generator2 generator3 = (Generator2) g3;
         assertThat(generator3.getConfig()).isSameAs(genCfg3);
         assertThat(generator3.getModel()).isEqualTo(parser2.getModel());
 
     }
 
     // CHECKSTYLE:ON
 
 }
