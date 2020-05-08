 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.se.index.lucene.impl;
 
 import org.apache.lucene.queryParser.QueryParser;
 
 import org.paxle.core.doc.Field;
 import org.paxle.se.query.IQueryFactory;
 import org.paxle.se.query.tokens.AToken;
 
 public class LuceneQueryFactory extends IQueryFactory<String> {
 	
 	private final Field<?>[] defaultFields;
 	
 	private boolean inFieldToken = false;
 	
 	public LuceneQueryFactory(final Field<?>... defaultFields) {
 		this.defaultFields = defaultFields;
 	}
 	
 	// make it synchronized, because we have to maintain an internal variable which may change
 	// during transformation
 	@Override
 	public synchronized String transformToken(AToken token) {
 		return super.transformToken(token);
 	}
 	
 	@Override
 	public void beginTransformation() {
 		inFieldToken = false;
 	}
 	
 	@Override
 	public void endTransformation() {
 		inFieldToken = false;
 	}
 	
 	private String getOperatorString(AToken[] children, String str) {
 		final StringBuilder sb = new StringBuilder();
 		sb.append('(');
 		for (int i=0; i<children.length; i++) {
			sb.append(transformToken(children[i], this));
 			if (i + 1 < children.length)
 				sb.append(' ').append(str).append(' ');
 		}
 		return sb.append(')').toString();
 	}
 	
 	
 	
 	@Override
 	public String and(AToken[] token) {
 		return getOperatorString(token, "AND");
 	}
 	
 	@Override
 	public String field(AToken token, Field<?> field) {
 		inFieldToken = true;
 		final String r = field.getName() + ':' + transformToken(token, this);
 		inFieldToken = false;
 		return r;
 	}
 	
 	@Override
 	public String mod(AToken token, String mod) {
 		return null;
 	}
 	
 	@Override
 	public String not(AToken token) {
 		return '-' + transformToken(token, this);
 	}
 	
 	@Override
 	public String or(AToken[] token) {
 		return getOperatorString(token, "OR");
 	}
 	
 	@Override
 	public String plain(String str) {
 		return toDefaultFields(QueryParser.escape(str));
 	}
 	
 	private String toDefaultFields(final String s) {
 		if (inFieldToken || defaultFields.length == 0) {
 			return s;
 		} else if (defaultFields.length == 1) {
 			return defaultFields[0].getName() + ':' + s;
 		} else {
 			final String sepTerms = " OR ";
 			
 			final StringBuilder sb = new StringBuilder((s.length() + 1) * defaultFields.length);
 			sb.append('(');
 			for (final Field<?> field : defaultFields)
 				sb.append(field.getName()).append(':').append(s).append(sepTerms);
 			final int len = sb.length();
 			return sb.delete(len - sepTerms.length(), len).append(')').toString();
 		}
 	}
 	
 	@Override
 	public String quote(String str) {
 		return toDefaultFields('"' + QueryParser.escape(str) + '"');
 	}
 }
