 package org.twuni.money.common;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.twuni.money.common.exception.ExpiredTokenException;
 import org.twuni.money.common.exception.InsufficientFundsException;
 import org.twuni.money.common.exception.ManyExceptions;
 import org.twuni.money.common.exception.NetworkException;
 
 public class Bank {
 
 	private final Repository<String, Token> vault;
 	private final Treasury treasury;
 
 	public Bank( Repository<String, Token> vault, Treasury treasury ) {
 		this.vault = vault;
 		this.treasury = treasury;
 	}
 
 	public int getBalance() {
 		int worth = 0;
 		for( Token token : vault.list() ) {
 			worth += token.getValue();
 		}
 		return worth;
 	}
 
 	public void deposit( Token token ) {
 
 		int value = treasury.getValue( token );
 
 		if( value <= 0 ) {
 			throw new ExpiredTokenException( token );
 		}
 
 		List<Token> tokens = vault.list( 1 );
 
 		if( tokens.isEmpty() ) {
 			if( value > 1 ) {
 				Token [] split = treasury.split( token, 1 ).toArray( new Token [0] );
 				token = treasury.merge( split[0], split[1] );
 			}
 			vault.save( token );
 		} else {
 			Token existing = tokens.toArray( new Token [0] )[0];
 			vault.save( treasury.merge( token, existing ) );
 			vault.delete( existing );
 		}
 
 	}
 
 	public Token withdraw( int amount ) {
 
 		List<Token> tokens = vault.list();
 
 		Collections.sort( vault.list() );
 
 		if( amount <= 0 ) {
 			throw new IllegalArgumentException();
 		}
 
 		if( amount > getBalance() ) {
 			throw new InsufficientFundsException();
 		}
 
 		Token token = tokens.remove( 0 );
 
 		while( token.getValue() < amount ) {
 			token = merge( token, tokens.remove( 0 ) );
 		}
 
 		if( token.getValue() > amount ) {
 
 			int change = token.getValue() - amount;
 
 			for( Token result : split( token, change ) ) {
 				if( result.getValue() == amount ) {
 					token = result;
 				}
 			}
 
 		}
 
 		return token;
 
 	}
 
 	public void validate() {
 
 		List<Exception> exceptions = new ArrayList<Exception>();
 
 		for( Token token : vault.list() ) {
 
 			try {
 
 				int worth = treasury.getValue( token );
 				if( worth <= 0 ) {
 					vault.delete( token );
 					exceptions.add( new ExpiredTokenException( token ) );
 				}
 
 			} catch( NetworkException exception ) {
 				exceptions.add( exception );
 			}
 
 		}
 
 		if( !exceptions.isEmpty() ) {
 			throw new ManyExceptions( exceptions );
 		}
 
 	}
 
 	private Set<Token> split( Token token, int amount ) {
 
 		Set<Token> split = treasury.split( token, amount );
 
 		vault.delete( token );
 		for( Token t : split ) {
 			vault.save( t );
 		}
 
 		return split;
 
 	}
 
 	private Token merge( Token a, Token b ) {
 
 		Token merged = treasury.merge( a, b );
 
 		vault.delete( a );
 		vault.delete( b );
 		vault.save( merged );
 
 		return merged;
 
 	}
 
 }
