 package hw3;
 
 class Converter {
 	
 	QueueAr infix;
 	
 	Converter( String infix )
 	{
 		try {
			this.infix = Converter.toQueue( infix );
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 //		this.infix.print();
 	}
 	
 	/**
 	 * converts the given string to a queue
 	 * @return queue
 	 * @throws Exception 
 	 */
 	private static QueueAr toQueue( String infix ) throws Exception
 	{
		QueueAr q = new QueueAr(infix.length() + 2);
 		String s = ""; // currently being processed string
 		String token;
 				
 		// wrap it
 		q.enqueue( "(" );
 		
 		for( int j = 0; j < infix.length(); j++ )
 		{
 			token = "" + infix.charAt( j );
 									
 			if( !Calculator.isOperand( token ) )
 			{
 				if( s != "" )
 				{
 					q.enqueue( s );
 				}
 				q.enqueue( token );
 				s = "";
 			}
 			else
 			{
 				s = s + token;
 			}
 		}
 		
 		q.enqueue( ")" );
		
 		return q;
 	}
 	
 	public QueueAr toPostFix() throws Exception {		
 		int len = this.infix.length();
 		
 		QueueAr s = new QueueAr( len ); // output stack
 		StackLi internal = new StackLi(); // stack for internal manipulation
 		
 		Object token;
 		while( !this.infix.isEmpty( ) )
 		{
 //            System.out.println( this.dequeue( ) );
 			token = this.infix.dequeue();
 		
 			if( Calculator.isOperand( (String) token ) )
 			{
 				s.enqueue( token ); // put the token onto the output stack
 			}
 			else if( token.equals( "(") )
 			{
 				internal.push( token );
 			}
 			else if( token.equals(")") )
 			{
 				while( !internal.isEmpty() && !internal.top().equals("("))
 				{
 					s.enqueue( internal.topAndPop() );
 				}
 				
 				// pop off the first paranthese
 				internal.pop();
 			}
 			else
 			{
				while( !internal.isEmpty() && Calculator.precedence( (String) token, (String) internal.top() ) && !internal.top().equals("(") )
 				{
 					s.enqueue( internal.topAndPop() );
 				}
 				internal.push( token );
 			}
 
 		}
 		
 		// add final bits
 		while( ! internal.isEmpty() )
 		{
 			s.enqueue( internal.topAndPop() );
 		}
 		
 		return s;
 		
 	}
 
 }
