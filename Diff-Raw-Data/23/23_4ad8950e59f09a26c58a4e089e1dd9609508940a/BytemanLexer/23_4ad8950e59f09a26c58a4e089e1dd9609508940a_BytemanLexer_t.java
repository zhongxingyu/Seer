 /*
  * Copyright (c) 2011-2012 Julien Nicoulaud <julien.nicoulaud@gmail.com>
  *
  * This file is part of idea-byteman.
  *
  * idea-byteman is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * idea-byteman is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with idea-byteman.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.nicoulaj.idea.byteman.lang.lexer;
 
 import com.intellij.lexer.FlexAdapter;
 import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

import static net.nicoulaj.idea.byteman.lang.BytemanTypes.QUOTED_IDENTIFIER;
import static net.nicoulaj.idea.byteman.lang.BytemanTypes.STRING_LITERAL;
 
 /**
  * {@link com.intellij.lexer.Lexer} implementation for Byteman.
  * <p/>
  * Uses {@link BytemanFlexLexer}, the JFlex lexer generated from {@code Byteman.flex}.
  *
  * @author <a href="mailto:julien.nicoulaud@gmail.com">Julien Nicoulaud</a>
  * @since 0.1
  */
 public class BytemanLexer extends MergingLexerAdapter {
 
    /** Token types for which two consecutives tokens are merged. */
    private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(STRING_LITERAL, QUOTED_IDENTIFIER);

     /** Build a new instance of {@link BytemanLexer}. */
     public BytemanLexer() {
        super(new FlexAdapter(new BytemanFlexLexer()), TOKENS_TO_MERGE);
     }
 }
