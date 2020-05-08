 package org.ocpsoft.tutorial.regex.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.enterprise.context.RequestScoped;
 
 import org.jboss.errai.bus.server.annotations.Service;
 import org.ocpsoft.tutorial.regex.client.shared.Group;
 import org.ocpsoft.tutorial.regex.client.shared.RegexParser;
 import org.ocpsoft.tutorial.regex.client.shared.RegexRequest;
 import org.ocpsoft.tutorial.regex.client.shared.RegexResult;
 import org.ocpsoft.tutorial.regex.server.ParseTools.CaptureType;
 import org.ocpsoft.tutorial.regex.server.ParseTools.CapturingGroup;
 
 @Service
 @RequestScoped
 public class RegexParserImpl implements RegexParser
 {
    @Override
    public RegexResult parse(RegexRequest request)
    {
       RegexResult result = new RegexResult();
       result.setText(request.getText());
 
       Matcher matcher = null;
 
       String regex = request.getRegex();
       if (request.getText() != null && !request.getText().isEmpty())
       {
          try {
             regex = javaMode(request.getRegex());
             if (regex != null)
             {
                matcher = Pattern.compile(regex).matcher(request.getText());
                result.setMatches(matcher.matches());
                if (request.getReplacement() != null && !request.getReplacement().isEmpty())
                   result.setReplaced(matcher.replaceAll(javaMode(request.getReplacement())));
             }
          }
          catch (Exception e) {
             result.setError(e.getMessage());
             return result;
          }
 
          List<CapturingGroup> captures = parseRegex(regex);
 
          matcher.reset();
         while (matcher.find())
          {
            Group defaultGroup = new Group(regex, matcher.start(), matcher.end());
            result.getGroups().add(defaultGroup);
            for (int i = 0; i < matcher.groupCount(); i++)
             {
               int start = matcher.start(i + 1);
               int end = matcher.end(i + 1);
               if (defaultGroup.getStart() != start || defaultGroup.getEnd() != end)
                {
                  result.getGroups().add(new Group(new String(captures.get(i).getCaptured()), start, end));
                }
             }
          }
 
          matcher.reset();
          if (matcher.matches())
          {
             result.getGroups().clear();
             for (int i = 0; i < matcher.groupCount(); i++)
             {
                result.getGroups().add(new Group(
                         new String(captures.get(i).getCaptured()),
                         matcher.start(i + 1),
                         matcher.end(i + 1))
                         );
             }
          }
       }
       return result;
    }
 
    private List<CapturingGroup> parseRegex(String regex)
    {
       List<CapturingGroup> captures = new ArrayList<ParseTools.CapturingGroup>();
       char[] chars = regex.toCharArray();
       if (chars.length > 0)
       {
          int cursor = 0;
          while (cursor < chars.length)
          {
             switch (chars[cursor])
             {
             case '(':
                CapturingGroup group = ParseTools.balancedCapture(chars, cursor, chars.length - 1, CaptureType.PAREN);
                captures.add(group);
 
                break;
 
             default:
                break;
             }
 
             cursor++;
          }
       }
       return captures;
    }
 
    public String javaMode(String regex)
    {
       StringBuilder result = new StringBuilder();
 
       int count = 0;
       for (int i = 0; i < regex.length(); i++) {
          char c = regex.charAt(i);
 
          if (c == '\\')
          {
             count++;
          }
 
          if (count % 2 == 1)
          {
             if (c != '\\')
             {
                if (i + 1 <= regex.length())
                {
                   throw new RegexException("Unterminated escape sequence at character " + i + ": " + result);
                }
             }
             else if (i + 1 == regex.length())
             {
                throw new RegexException("Unterminated escape sequence at character " + i + ": " + result);
             }
          }
          result.append(c);
       }
       return result.toString().replaceAll("\\\\\\\\", "\\\\");
    }
 }
