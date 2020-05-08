 package edu.kit.pp.minijava;
 
 import edu.kit.pp.minijava.tokens.Token;
 import java.io.IOException;
 
 class PeekingLexer {
 
 	private class Position {
 
 		int _line, _column;
 
 		public Position(int line, int column) {
 			_line = line;
 			_column = column;
 		}
 
 		public int getLine() {
 			return _line;
 		}
 
 		public int getColumn() {
 			return _column;
 		}
 	}
 	private Lexer _lexer;
 	private int _size;
 	private Token[] _lookAhead;
 	private Position[] _lookAheadPos;
 	private int _pos;
 	private Position _currentPos;
 	private Token _currentToken;
 
 	public PeekingLexer(Lexer lexer, int size) throws IOException {
 		_lexer = lexer;
 		_size = size;
 		_pos = 0;
 		_lookAheadPos = new Position[size];
 		_lookAhead = new Token[size];
 		for (int i = 0; i < size; i++) {
 			
 			_lookAhead[i] = _lexer.next();
 			_lookAheadPos[i] = new Position(_lexer.getLine(), _lexer.getColumn());
 		}
 		_currentPos = _lookAheadPos[0];
 		_currentToken = _lookAhead[0];
 	}
 
 	public Token next() throws IOException {
 		int i = _pos % _size;
 		Token result = _lookAhead[i];
 		// save old position
 		_currentPos = _lookAheadPos[i];
 		_currentToken = _lookAhead[i];
 		
		_lookAhead[i] = _lexer.next();
		_lookAheadPos[i] = new Position(_lexer.getLine(), _lexer.getColumn());
 		
 		_pos += 1;
 		if (_pos >= _size) {
 			_pos = 0;
 		}
 		return result;
 	}
 
 	Token peek(int i) {
 		return _lookAhead[(_pos + i) % _size];
 	}
 
 	int getCurrentLine() {
 		return getLine(0);
 	}
 
 	int getCurrentColumn() {
 		return getColumn(0);
 	}
 
 	int getLine(int i) {
 		if (0 == i) {
 			return _currentPos.getLine();
 		}
 
 		return _lookAheadPos[(_pos + i - 1) % _size].getLine();
 	}
 
 	int getColumn(int i) {
 		if (0 == i) {
 			return _currentPos.getColumn() - _currentToken.getValue().length();
 		}
 
 		return _lookAheadPos[(_pos + i - 1) % _size].getColumn() - 
 				_lookAhead[(_pos + 1 - 1) % _size].getValue().length();
 	}
 }
