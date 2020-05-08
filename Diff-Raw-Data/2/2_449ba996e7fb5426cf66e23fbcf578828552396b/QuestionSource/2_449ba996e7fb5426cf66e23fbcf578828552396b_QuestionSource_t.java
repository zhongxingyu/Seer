 package org.nonstdout.askme;
 
 import org.nonstdout.askme.Question;
 import org.nonstdout.askme.QuestionPack;
 
 import android.content.res.AssetManager;
 import android.content.Context;
 import android.util.Log;
 
 import java.util.Vector;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class QuestionSource
 {
   public static final String TAG = "Askme";
   public static final String SOURCE_DIR = "questions";
 
   public QuestionSource(Context context)
   {
     context_ = context;
   }
 
   public Vector<QuestionPack> question_packs()
     throws java.io.IOException
   {
     AssetManager am = context_.getResources().getAssets();
     String[] paths = am.list(SOURCE_DIR);
 
     Vector<QuestionPack> question_packs = new Vector<QuestionPack>();
 
     for (String path: paths)
     {
       StringBuffer full_path = new StringBuffer();
       full_path.append(SOURCE_DIR);
       full_path.append("/");
       full_path.append(path);
 
       QuestionPack question_pack = new QuestionPack(
           this,
           path,
           full_path.toString());
       question_packs.add(question_pack);
     }
 
     return question_packs;
   }
 
   private static final Pattern QUESTION_PATTERN = Pattern.compile(
       "(?i)^Question:\\s*(.*)");
 
   private static final Pattern ANSWER_PATTERN = Pattern.compile(
       "(?i)^Answer:\\s*(.*)");
  
   private static final Pattern COMMENT_PATTERN = Pattern.compile(
       "^(\\s*|#.*)$");
 
   private static final Pattern TRANSLITERATE_PATTERN = Pattern.compile(
       "(.*)\\{(.*?)\\}(.*)");
 
   public String translate(String str)
   {
     Matcher m;
     while ((m = TRANSLITERATE_PATTERN.matcher(str)).matches())
     {
       String substr = m.group(2).replaceAll("(.)", "$1 ");
       substr = substr.replaceAll("'''", "triple prime");
       substr = substr.replaceAll("''", "double prime");
       substr = substr.replaceAll("'", "prime");
       substr = substr.replaceAll("-", "minus");
      substr = substr.replaceAll("\\+", "plus");
       str = m.group(1) + " \"" + substr + "\" " + m.group(3);
     }
     return str;
   }
 
   public Vector<Question> read_question_pack(QuestionPack question_pack)
     throws java.io.IOException
   {
     AssetManager am = context_.getResources().getAssets();
     InputStream in = am.open(question_pack.path());
     InputStreamReader isr = new InputStreamReader(in);
     BufferedReader reader = new BufferedReader(isr);
 
     Vector<Question> questions = new Vector<Question>();
 
     StringBuffer current_question = new StringBuffer();
     StringBuffer current_answer = new StringBuffer();
     StringBuffer append_to = new StringBuffer();
 
     String line;
     while ((line = reader.readLine()) != null)
     {
       Matcher m;
 
       if ((m = QUESTION_PATTERN.matcher(line)).matches())
       {
         if (current_question.length() != 0)
         {
           Question question = new Question(
               translate(current_question.toString()),
               translate(current_answer.toString()));
           questions.add(question);
         }
 
         current_question = new StringBuffer(m.group(1));
         current_answer = new StringBuffer();
         append_to = current_question;
       }
       else if ((m = ANSWER_PATTERN.matcher(line)).matches())
       {
         current_answer = new StringBuffer(m.group(1));
         append_to = current_answer;
       }
       else if ((m = COMMENT_PATTERN.matcher(line)).matches())
       {
         // ignore
       }
       else
       {
         append_to.append(line);
       }
     }
 
     if (current_question.length() != 0)
     {
       Question question = new Question(
           translate(current_question.toString()),
           translate(current_answer.toString()));
       questions.add(question);
     }
 
     in.close();
 
     return questions;
   }
 
   private Context context_;
 }
 
