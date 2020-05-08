 /*
  * Copyright 2002-2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package biz.c24.io.spring.sink;
 
 import java.io.ByteArrayOutputStream;
 import java.io.StringWriter;
 
import org.springframework.xml.xpath.XPathExpression;
import org.w3c.dom.Node;

 import biz.c24.io.api.presentation.Sink;
 
 /**
  * 
  */
 public enum OutputType {
 
 	STRING {
 
 		@Override
 		public Sink getSink(SinkFactory factory) {
 			return factory.createSink(new StringWriter());
 		}
 
 		@Override
 		public Object getOutput(Sink sink) {
 			return sink.getWriter().toString();
 		}
 
 	},
 
 	BYTE_ARRAY {
 
 		@Override
 		public Sink getSink(SinkFactory factory) {
 			return factory.createSink(new ByteArrayOutputStream());
 		}
 
 		@Override
 		public Object getOutput(Sink sink) {
 			ByteArrayOutputStream baos = (ByteArrayOutputStream) sink
 					.getOutputStream();
 			return baos.toByteArray();
 		}
 	};
 
 	public abstract Sink getSink(SinkFactory factory);
 
 	public abstract Object getOutput(Sink sink);
 
 }
