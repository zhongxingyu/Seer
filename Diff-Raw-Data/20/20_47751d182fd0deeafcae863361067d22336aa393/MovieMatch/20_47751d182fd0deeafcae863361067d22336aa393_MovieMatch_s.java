 package com.fsu.kevinfriedpig;
 
 public class MovieMatch{
 	
 	String baseActor_ = new String();
 	SymGraph sg_ = new SymGraph();
 	boolean actorExistsInDataBase = false;
 	boolean baseActorExistsInDataBase = false;
 	boolean connectedToBaseActor = false;
 	
 	
 	MovieMatch ( String baseActor)
 	//constructor
 	{
 	  //int length = baseActor.length();
 	  baseActor_ = baseActor;	
 	  //baseActor_[length] = '\0';
 	  //strcpy (baseActor_,baseActor);
 	}
 
 	void Load (String filename)
 	// loads a moview/actor table
 	{
 	  std::ifstream movieFile;
 	  movieFile.open(filename);
 	  if (!movieFile.is_open())
 	  {
 	    std::cout << " ** Error: cannot open file " << filename << '\n';
 	    return;
 	  }
 	  
 	  //read 1 for size
 	  size_t vertexSize = 0;
 	  while (!movieFile.eof())
 	  {
 	    fsu::String line;
 	    line.GetLine(movieFile);
 	    size_t index = 0;
 	    
 	    while( index < line.Length() )
 	    {
 	      for(; index < line.Length(); ++index)
 	      {//interate through line from index to first '/'
 	        if ( line[index] == '/' || line[index] == '\0')
 	          ++vertexSize;
 	        else if ( line[index] == '\n' )
 	        {
 	          ++vertexSize;
 	          break;
 	        }
 	      }
 	    }
 	  }
 	  sg_.SetVrtxSize(vertexSize);
 	  
 	  movieFile.close();
 	  
 	  //read 2 for the numbers
 	  movieFile.open(filename);
 	  //take in one line at a time until end of file.
 	  while (!movieFile.eof())
 	  {
 	    size_t startIndex = 0,
 	    index = 0;
 	    bool gotMovie = false,
 	    eol = false;
 	    fsu::String line;
 	    std::string movie;
 	    line.GetLine(movieFile);
 	    
 	    //iterate though movie titles or actor names by using '/' to delimit a "token"
 	    while(index < line.Length() )
 	    {
 	      std::string actor;
 	      startIndex = index;
 	      for(; index < line.Length(); ++index)
 	      {//interate through line from index to first '/'
 	        if ( line[index] == '/' )
 	        {
 	          break;
 	        }
 	        else if ( line[index] == '\0' )
 	        {
 	          eol = true;
 	          break;
 	        }
 	      }
 	      if ( !gotMovie )
 	      {//if we havent gotten a movie yet get movie title
 	        for( size_t j = startIndex; j < index; ++j )
 	          movie += line[j];
 	        
 	        movie += '\0';
 	        sg_.Push( movie );
 	        ++index;
 	        gotMovie = true;
 	      }
 	      else
 	      {//else get actor name
 	        
 	        std::string lastName;
 	        bool gotLast = false;
 	        bool dumpEnd = false;
 	        
 	        for( size_t j = startIndex; j < index; ++j )
 	        {
 	          if( line[j] != ',' && !gotLast )
 	            lastName += line[j];
 	          else if ( line [j] == ',' ) // ditch comma and the following space
 	          {
 	            gotLast = true;
 	            ++j;
 	          }
 	          else if ( gotLast && line[j] == ' ' &&  line[j+1] == '(' ) // ditch the spaces after FN
 	            gotLast = true;
 	          else if ( gotLast && line[j] != '(' && !dumpEnd )
 	            actor += line[j];
 	          else if ( line[j] == '(' )
 	            dumpEnd = true;
 	          else if ( dumpEnd )
 	            break;
 	          else
 	            std::cout << " *** ERROR: Actor name couldn't be interpreted\n";
 	        }
 	        actor += ' ';
 	        actor += lastName;
 	        
 	        actor += '\0';
 	        sg_.Push( actor );
 	        sg_.AddEdge( movie, actor );
 	        ++index;
 	      }    } //while (!eol)
 	  } // while (!movieFile.eof())
 	  movieFile.close();
 	}  // load ( const char * filename)
 
 
 	int MovieDistance(String actor)
 	// returns the number of movies required to get from actor to baseActor_
 	{
 	  BfSurvey bfs = new BfSurvey( sg_.g_ );
 	    
 	  //String actorPtr;
 	  //String baseActorPtr;
	  int i = 0;
 //	  while(!actor.isEmpty())
 //	  {
 //	    actorPtr += actor[i];
 //	    ++i;
 //	  }
 //	  actorPtr += '\0';
 	  
 	  
	  if (sg_.s2n_.Includes(actorPtr) == sg_.s2n_.End() )
 	  {
 	    //std::cout << actor << " does not exist in the database.  Try again.\n";
 		  actorExistsInDataBase = false;
 		  return 0;
 	  }
 	  
	  int j = 0;
 //	  while(baseActor_[j] != '\0' && baseActor_[j] != '\n')
 //	  {
 //	    baseActorPtr += baseActor_[j];
 //	    ++j;
 //	  }
 //	  baseActorPtr += '\0';
 	  
	  if (sg_.s2n_.Includes(baseActorPtr) == sg_.s2n_.End() )
 	  {
 	    //std::cout << baseActor_ << " does not exist in the database.  Try again.\n";
 		  baseActorExistsInDataBase = false;
 		  return 0;
 	  }
 	  
	  bfs.Search(sg_.s2n_.get(baseActor_);
 	  
	  if ( bfs.Distance()[sg_.s2n_[actorPtr]] >= sg_.VrtxSize() )
 	  {
 	    //std::cout << actorPtr << " has no shared path to "
 	    //<< baseActorPtr << " \n";
 		  connectedToBaseActor = false;
 	    return -1;
 	  }
 	  
	  return bfs.Distance()[sg_.s2n_[actorPtr]] / 2;
 	}
 
 }
