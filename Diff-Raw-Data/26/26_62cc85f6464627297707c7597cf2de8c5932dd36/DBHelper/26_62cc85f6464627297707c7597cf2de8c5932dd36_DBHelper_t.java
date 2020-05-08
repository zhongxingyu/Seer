 
 package com.gec.questoesGratis.dao;
 
 import static com.gec.questoesGratis.tools.ListX.randomStart;
 import static com.gec.questoesGratis.tools.ListX.shuffle;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import com.gec.questoesGratis.model.Answer;
 import com.gec.questoesGratis.model.Filter;
 import com.gec.questoesGratis.model.Filter.Ignore;
 import com.gec.questoesGratis.model.Qualifier;
 import com.gec.questoesGratis.model.Question;
 import com.gec.questoesGratis.model.Quiz;
 import com.gec.questoesGratis.model.Quiz.Status;
 import com.gec.questoesGratis.tools.LogX;
 
 /**
  * Database Helper class.
  * 
  * @author agodinho
  */
 public final class DBHelper extends SQLiteOpenHelper {
 
    private static final LogX             log = new LogX( DBHelper.class );
    private static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
 
    private final Context                 context;
 
    private SQLiteDatabase                database;
 
    public DBHelper( Context contextP ) {
       super( contextP, DBProperties.DB_NAME, null, DBProperties.DB_VERSION );
       context = contextP;
    }
 
    @Override
    public void onCreate( SQLiteDatabase database ) {
       // database.execSQL( DBProperties.DDL_CREATE );
    }
 
    @Override
    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
       // log.w( "Upgrading database from version {0} to {1}, which will destroy all old data ...", oldVersion, newVersion );
       // database.execSQL( DBProperties.DDL_DROP );
       // onCreate( database );
    }
 
    public void openDataBase() throws SQLException {
       database = this.getWritableDatabase();
    }
 
    @Override
    public synchronized void close() {
       if( database != null )
          database.close();
       super.close();
       log.d( "Database connection closed." );
    }
 
    public void createDataBase() throws IOException {
 
       if( !databaseExists() ) {
 
          // By calling this method and empty database will be created into
          // the default system path of your application so we are gonna be
          // able to overwrite that database with our database.
          this.getReadableDatabase();
 
          try {
             copyDataBase();
          } catch( IOException e ) {
             throw new Error( "Error copying database" );
          }
 
          log.d( "Database created." );
       }
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     * 
     * @return true if it exists, false if it doesn't
     */
    boolean databaseExists() {
 
       boolean exists = true;
 
       try {
          final SQLiteDatabase db = SQLiteDatabase.openDatabase( //
                DBProperties.DB_FILENAME, //
                null, //
                SQLiteDatabase.OPEN_READONLY );
          db.close();
 
       } catch( SQLiteException e ) {
          exists = false;
       }
 
       if( exists )
          log.d( "Database ALREADY exists." );
       else
          log.d( "Database DOES NOT exists." );
 
       return exists;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    void copyDataBase() throws IOException {
 
       // Open your local db as the input stream.
       final InputStream is = context.getAssets().open( DBProperties.DB_NAME );
 
       // Open the empty db as the output stream.
       final OutputStream os = new FileOutputStream( DBProperties.DB_FILENAME );
 
       // Transfer bytes from the input into the output file.
       byte[] buffer = new byte[ 1024 ];
       int length;
       while( ( length = is.read( buffer ) ) > 0 ) {
          os.write( buffer, 0, length );
       }
 
       // Close the streams.
       os.flush();
       os.close();
       is.close();
    }
 
    // --- System Specific -------------------------------------------------- //
 
    public List< String > getBancas() {
       return getNames( DBProperties.SQL_SELECT_Bancas );
    }
 
    public List< String > getAnos() {
       return getNames( DBProperties.SQL_SELECT_Anos );
    }
 
    public List< String > getOrgaos() {
       return getNames( DBProperties.SQL_SELECT_Orgaos );
    }
 
    public List< String > getUFs() {
       return getNames( DBProperties.SQL_SELECT_UFs );
    }
 
    public List< String > getCargos() {
       return getNames( DBProperties.SQL_SELECT_Cargos );
    }
 
    public List< String > getDisciplinas() {
       return getNames( DBProperties.SQL_SELECT_Disciplinas );
    }
 
    public List< String > getAssuntos() {
       return getNames( DBProperties.SQL_SELECT_Assuntos );
    }
 
    static StringBuffer getWhere( Filter filter ) {
 
       final StringBuffer b = new StringBuffer();
       final Ignore ignore = filter.getIgnore();
       if( Ignore.ANSWERED.equals( ignore ) ) {
          addAnd( b ).append( "used IS NULL" );
       } else if( Ignore.RIGHT.equals( ignore ) ) {
          addAnd( b ).append( "substr(coalesce(used,'--'),1,1)!='r'" );
       } else if( Ignore.WRONG.equals( ignore ) ) {
          addAnd( b ).append( "substr(coalesce(used,'--'),2,1)!='w'" );
       }
       addInClause( b, "trim(banca)", filter.getBancas() );
       addInClause( b, "trim(ano)", filter.getAnos() );
       addInClause( b, "trim(orgao)", filter.getOrgaos() );
       addInClause( b, "trim(uf)", filter.getUFs() );
       addInClause( b, "trim(cargo)", filter.getCargos() );
       addInClause( b, "trim(disciplina)", filter.getDisciplinas() );
       addInClause( b, "trim(assunto)", filter.getAssuntos() );
       return b;
    }
 
    static StringBuffer getRawWhere( Filter filter ) {
 
       final StringBuffer b = new StringBuffer();
       final StringBuffer where = getWhere( filter );
       if( where.length() > 0 )
          b.append( "WHERE " ) //
                .append( where );
       return b;
    }
 
    public int getQuestionsCount( Filter filter ) {
 
       final StringBuffer b = //
       new StringBuffer( "SELECT Count(1) FROM questions " ) //
             .append( getRawWhere( filter ) );
       final Cursor c = database.rawQuery( b.toString(), null );
       c.moveToFirst();
       int count = c.getInt( 0 );
       c.close();
       return count;
    }
 
    public Quiz createQuiz( Filter filter ) {
 
       database.beginTransaction();
       try {
          final Quiz quiz = new Quiz( filter.getDescription() );
          insertQuiz( quiz );
          quiz.setAnswers( createAnswers( quiz, filter ) );
          insertAnswers( quiz );
          database.setTransactionSuccessful();
 
          final Long quizId = quiz.getId();
          final Quiz quiz2 = getQuiz( quizId );
          quiz2.setAnswers( getAnswers( quizId ) );
          return quiz2;
 
       } catch( SQLException e ) {
          throw new Error( "Cant create the quiz" );
       } finally {
          database.endTransaction();
       }
    }
 
    public List< Quiz > getQuizzes() {
 
       final List< Quiz > list = new ArrayList< Quiz >();
 
       final Cursor c = database.query( //
             /* table.. */"quizzes", //
             /* columns */null, //
             /* where.. */null, //
             /* whereA. */null, //
             /* groupBy */null, //
             /* having. */null, //
             /* orderBy */null //
             );
 
       while( c.moveToNext() )
          list.add( getQuiz( c ) );
       c.close();
 
       return list;
    }
 
    public Quiz getQuiz( Long quizId ) {
 
       final Cursor c = database.query( //
             /* table.. */"quizzes", //
             /* columns */null, //
             /* where.. */"_id=?", //
             /* whereA. */new String[] { quizId.toString() }, //
             /* groupBy */null, //
             /* having. */null, //
             /* orderBy */null //
             );
 
       final Quiz quiz = c.moveToNext()? getQuiz( c ): null;
       c.close();
 
       return quiz;
    }
 
    public List< Answer > getAnswers( Long quizId ) {
 
       final List< Answer > list = new ArrayList< Answer >();
 
       final Cursor c = database.query( //
             /* table.. */"vw_answers", //
             /* columns */null, //
             /* where.. */"quizId=?", //
             /* whereA. */new String[] { quizId.toString() }, //
             /* groupBy */null, //
             /* having. */null, //
             /* orderBy */"number" //
       );
 
       while( c.moveToNext() )
          list.add( getAnswer( c ) );
       c.close();
 
       return list;
    }
 
    public void updateAnswer( Long answerId, String answer ) {
 
       final ContentValues values = new ContentValues();
       values.put( "answer", answer );
 
       database.update( //
             /* table. */"answers", //
             /* values */values, //
             /* where. */"_id=?", //
             /* whereA */new String[] { answerId.toString() } );
 
       /*/ trg_answers_au -> update -> questions /*/
       /*/ trg_answers_au -> update -> quizzes   /*/
    }
 
    // --- PRIVATE CODE ----------------------------------------------------- //
 
    private List< String > getNames( String sql, String columnName ) {
 
       final List< String > list = new ArrayList< String >();
 
       final Cursor cursor = database.rawQuery( sql, null );
       if( cursor != null ) {
 
          final int nameIdx = cursor.getColumnIndex( columnName );
          while( cursor.moveToNext() ) {
             final String name = cursor.getString( nameIdx );
             list.add( name );
          }
          cursor.close();
       }
 
       return list;
    }
 
    private List< String > getNames( String sql ) {
       return getNames( sql, "name" );
    }
 
    private static StringBuffer getPlick( List< String > list ) {
 
       final StringBuffer b = new StringBuffer();
 
       if( list != null )
          for( String item : list )
             b.append( "'" ) //
                   .append( item.trim() ) //
                   .append( "'" ) //
                   .append( "," );
 
       final int l = b.length();
       if( l > 0 )
          b.deleteCharAt( l - 1 );
 
       return b;
    }
 
    private static StringBuffer getIn( String field, List< String > list ) {
 
       final StringBuffer b = new StringBuffer();
 
       final StringBuffer in = getPlick( list );
       if( in.length() > 0 ) //
          b.append( field ) //
                .append( " IN( " ) //
                .append( in ) //
                .append( " ) " );
 
       return b;
    }
 
    private static StringBuffer addAnd( StringBuffer b ) {
       if( b.length() > 0 )
          b.append( "AND " );
       return b;
    }
 
    private static void addInClause( StringBuffer b, String field, List< String > list ) {
 
       final StringBuffer in = getIn( field, list );
       if( in.length() > 0 )
          addAnd( b ).append( in );
    }
 
    private void insertQuiz( Quiz quiz ) {
 
       final ContentValues values = new ContentValues();
       values.put( "filter", quiz.getFilter() );
       final Long id = database.insertOrThrow( "quizzes", null, values );
       quiz.setId( id );
    }
 
    private List< Answer > createAnswers( Quiz quiz, Filter filter ) {
 
       final int total = filter.getTotal();
       final List< Answer > list = new ArrayList< Answer >( total );
 
       final List< Long > ids = getQuestionsId( filter );
       shuffle( ids );
      final int startPos = randomStart( ids, total );
      final int size = ids.size();
      final int finalPos = size < total? size: total;
      for( int n = 0; n < finalPos; n++ ) {
          final Question q = new Question();
         q.setId( ids.get( startPos + n ) );
          final Answer a = new Answer();
          a.setQuestion( q );
          a.setNumber( n + 1 );
          list.add( a );
       }
 
       return list;
    }
 
    private void insertAnswers( Quiz quiz ) {
 
       //TODO: sqLite does have support for the insert batch?
       final Long quizId = quiz.getId();
       final List< Answer > list = quiz.getAnswers();
       if( list != null )
          for( Answer answer : list )
             insertAnswer( quizId, answer );
    }
 
    private void insertAnswer( Long quizId, Answer answer ) {
 
       final ContentValues values = new ContentValues();
       values.put( "quizId", quizId );
       values.put( "questionId", answer.getQuestionId() );
       values.put( "number", answer.getNumber() );
       final Long id = database.insertOrThrow( "answers", null, values );
       /*/ trg_answers_ai -> update -> questions /*/
       answer.setId( id );
    }
 
    private List< Long > getQuestionsId( Filter filter ) {
 
       final List< Long > list = new ArrayList< Long >();
 
       final Cursor c = database.query( //
             /* table.. */"questions", //
             /* columns */new String[] { "_id" }, //
             /* where.. */getWhere( filter ).toString(), //
             /* whereA. */null, //
             /* groupBy */null, //
             /* having. */null, //
             /* orderBy */null //
             );
 
       while( c.moveToNext() )
          list.add( new Long( c.getLong( 0 ) ) );
       c.close();
 
       return list;
    }
 
    private Quiz getQuiz( Cursor c ) {
 
       final Quiz q = new Quiz();
       q.setId( c.getLong( EQuizzes_id ) );
       q.setDate( getDate( c, EQuizzes_date ) );
       q.setFilter( c.getString( EQuizzes_filter ) );
       q.setRating( c.getInt( EQuizzes_rating ) );
       q.setStatus( Status.valueOf( c.getInt( EQuizzes_status ) ) );
       q.setLastNumber( c.getInt( EQuizzes_lastNumber ) );
       return q;
    }
 
    private Answer getAnswer( Cursor c ) {
 
       final Answer a = new Answer();
       a.setId( c.getLong( EAnswers_id ) );
       a.setNumber( c.getInt( EAnswers_number ) );
       a.setAnswer( c.getString( EAnswers_answer ) );
       a.setQuestion( getQuestion( c ) );
       return a;
    }
 
    private Question getQuestion( Cursor c ) {
 
       final Question q = new Question();
       q.setId( c.getLong( EQuestions_id ) );
 
       final Qualifier x = new Qualifier();
       x.setBanca( c.getString( EQuestions_banca ) );
       x.setAno( c.getString( EQuestions_ano ) );
       x.setOrgao( c.getString( EQuestions_orgao ) );
       x.setUF( c.getString( EQuestions_uf ) );
       x.setCargo( c.getString( EQuestions_cargo ) );
       x.setDisciplina( c.getString( EQuestions_disciplina ) );
       x.setAssunto( c.getString( EQuestions_assunto ) );
       q.setQualifier( x );
 
       q.setDescription( c.getString( EQuestions_question ) );
 
       final List< String > options = new ArrayList< String >();
 
       for( int i = 0; i < EQuestions_MAX; i++ ) {
          final String option = c.getString( EQuestions_optionA + i );
          if( option != null )
             options.add( option );
       }
       q.setOptions( options );
 
       q.setMatch( c.getString( EQuestions_match ) );
       q.setUsed( c.getString( EQuestions_used ) );
       return q;
    }
 
    private static Date getDate( Cursor c, int index ) {
       try {
          return sdf.parse( c.getString( index ) );
       } catch( ParseException e ) {
          return null;
       }
    }
 
    private static final int EQuestions_MAX        = 5;
    private static final int EQuestions_id         = 0;
    private static final int EQuestions_banca      = 1;
    private static final int EQuestions_ano        = 2;
    private static final int EQuestions_orgao      = 3;
    private static final int EQuestions_uf         = 4;
    private static final int EQuestions_cargo      = 5;
    private static final int EQuestions_disciplina = 6;
    private static final int EQuestions_assunto    = 7;
    private static final int EQuestions_question   = 8;
    private static final int EQuestions_optionA    = 9;
    //private static final int EQuestions_optionB  = 10;
    //private static final int EQuestions_optionC  = 11;
    //private static final int EQuestions_optionD  = 12;
    //private static final int EQuestions_optionE  = 13;
    private static final int EQuestions_match      = 14;
    private static final int EQuestions_used       = 15;
    //private static final int EAnswers_quizid     = 16;
    private static final int EAnswers_id           = 17;
    private static final int EAnswers_number       = 18;
    private static final int EAnswers_answer       = 19;
 
    private static final int EQuizzes_id           = 0;
    private static final int EQuizzes_date         = 1;
    private static final int EQuizzes_filter       = 2;
    private static final int EQuizzes_rating       = 3;
    private static final int EQuizzes_status       = 4;
    private static final int EQuizzes_lastNumber   = 5;
 }
