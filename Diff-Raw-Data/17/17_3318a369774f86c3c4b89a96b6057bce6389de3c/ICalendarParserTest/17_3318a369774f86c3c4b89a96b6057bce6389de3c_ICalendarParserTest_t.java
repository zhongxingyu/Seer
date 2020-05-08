 package ical.parser;
 
 import org.antlr.v4.runtime.ANTLRInputStream;
 import org.antlr.v4.runtime.CommonTokenStream;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class ICalendarParserTest {
 
     private ICalendarParser getParser(String source) {
         ICalendarLexer lexer = new ICalendarLexer(new ANTLRInputStream(source));
         return new ICalendarParser(new CommonTokenStream(lexer));
     }
 
     @Test
     public void testIcalobject() {
 
         // icalobject
         //  : k_begin COL k_vcalendar CRLF
         //    calprop*?
         //    component+?
         //    k_end COL k_vcalendar CRLF
         //  ;
 
         String source = "BEGIN:VCALENDAR\n" +
                 "PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN\n" +
                 "VERSION:2.0\n" +
                 "BEGIN:VEVENT\n" +
                 "DTSTAMP:19960704T120000Z\n" +
                 "UID:uid1@example.com\n" +
                 "ORGANIZER:mailto:jsmith@example.com\n" +
                 "DTSTART:19960918T143000Z\n" +
                 "DTEND:19960920T220000Z\n" +
                 "STATUS:CONFIRMED\n" +
                 "CATEGORIES:CONFERENCE\n" +
                 "SUMMARY:Networld+Interop Conference\n" +
                 "DESCRIPTION:Networld+Interop Conference\n" +
                 " and Exhibit\\nAtlanta World Congress Center\\n\n" +
                 " Atlanta\\, Georgia\n" +
                 "END:VEVENT\n" +
                 "END:VCALENDAR\n";
         ICalendarParser.IcalobjectContext ctx = getParser(source).icalobject();
        assertThat( ctx.getText().trim(), is(source.replace("\n ", "").trim()) );
         assertThat( ctx.calprop().size(), is(2) );
         assertThat( ctx.component().size(), is(1) );
 
         source = "BEGIN:VCALENDAR\n" +
                 "PRODID:-//RDU Software//NONSGML HandCal//EN\n" +
                 "VERSION:2.0\n" +
                 "BEGIN:VTIMEZONE\n" +
                 "TZID:America/New_York\n" +
                 "BEGIN:STANDARD\n" +
                 "DTSTART:19981025T020000\n" +
                 "TZOFFSETFROM:-0400\n" +
                 "TZOFFSETTO:-0500\n" +
                 "TZNAME:EST\n" +
                 "END:STANDARD\n" +
                 "BEGIN:DAYLIGHT\n" +
                 "DTSTART:19990404T020000\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "END:VTIMEZONE\n" +
                 "BEGIN:VEVENT\n" +
                 "DTSTAMP:19980309T231000Z\n" +
                 "UID:guid-1.example.com\n" +
                 "ORGANIZER:mailto:mrbig@example.com\n" +
                 "ATTENDEE;RSVP=TRUE;ROLE=REQ-PARTICIPANT;CUTYPE=GROUP:\n" +
                 " mailto:employee-A@example.com\n" +
                 "DESCRIPTION:Project XYZ Review Meeting\n" +
                 "CATEGORIES:MEETING\n" +
                 "CLASS:PUBLIC\n" +
                 "CREATED:19980309T130000Z\n" +
                 "SUMMARY:XYZ Project Review\n" +
                 "DTSTART;TZID=America/New_York:19980312T083000\n" +
                 "DTEND;TZID=America/New_York:19980312T093000\n" +
                 "LOCATION:1CP Conference Room 4350\n" +
                 "END:VEVENT\n" +
                 "END:VCALENDAR\n";
         ctx = getParser(source).icalobject();
        assertThat( ctx.getText().trim(), is(source.replace("\n ", "").trim()) );
         assertThat( ctx.calprop().size(), is(2) );
         assertThat( ctx.component().size(), is(2) );
 
         source = "BEGIN:VCALENDAR\n" +
                 "METHOD:xyz\n" +
                 "VERSION:2.0\n" +
                 "PRODID:-//ABC Corporation//NONSGML My Product//EN\n" +
                 "BEGIN:VEVENT\n" +
                 "DTSTAMP:19970324T120000Z\n" +
                 "SEQUENCE:0\n" +
                 "UID:uid3@example.com\n" +
                 "ORGANIZER:mailto:jdoe@example.com\n" +
                 "ATTENDEE;RSVP=TRUE:mailto:jsmith@example.com\n" +
                 "DTSTART:19970324T123000Z\n" +
                 "DTEND:19970324T210000Z\n" +
                 "CATEGORIES:MEETING,PROJECT\n" +
                 "CLASS:PUBLIC\n" +
                 "SUMMARY:Calendaring Interoperability Planning Meeting\n" +
                 "DESCRIPTION:Discuss how we can test c&s interoperability\\n\n" +
                 " using iCalendar and other IETF standards.\n" +
                 "LOCATION:LDB Lobby\n" +
                 "ATTACH;FMTTYPE=application/postscript:ftp://example.com/pub/\n" +
                 " conf/bkgrnd.ps\n" +
                 "END:VEVENT\n" +
                 "END:VCALENDAR\n";
         ctx = getParser(source).icalobject();
        assertThat( ctx.getText().trim(), is(source.replace("\n ", "").trim()) );
         assertThat( ctx.calprop().size(), is(3) );
         assertThat( ctx.component().size(), is(1) );
 
         source = "BEGIN:VCALENDAR\n" +
                 "VERSION:2.0\n" +
                 "PRODID:-//ABC Corporation//NONSGML My Product//EN\n" +
                 "BEGIN:VTODO\n" +
                 "DTSTAMP:19980130T134500Z\n" +
                 "SEQUENCE:2\n" +
                 "UID:uid4@example.com\n" +
                 "ORGANIZER:mailto:unclesam@example.com\n" +
                 "ATTENDEE;PARTSTAT=ACCEPTED:mailto:jqpublic@example.com\n" +
                 "DUE:19980415T000000\n" +
                 "STATUS:NEEDS-ACTION\n" +
                 "SUMMARY:Submit Income Taxes\n" +
                 "BEGIN:VALARM\n" +
                 "ACTION:AUDIO\n" +
                 "TRIGGER:19980403T120000Z\n" +
                 "ATTACH;FMTTYPE=audio/basic:http://example.com/pub/audio-\n" +
                 " files/ssbanner.aud\n" +
                 "REPEAT:4\n" +
                 "DURATION:PT1H\n" +
                 "END:VALARM\n" +
                 "END:VTODO\n" +
                 "END:VCALENDAR\n";
         ctx = getParser(source).icalobject();
        assertThat( ctx.getText().trim(), is(source.replace("\n ", "").trim()) );
         assertThat( ctx.calprop().size(), is(2) );
         assertThat( ctx.component().size(), is(1) );
 
         source = "BEGIN:VCALENDAR\n" +
                 "VERSION:2.0\n" +
                 "PRODID:-//ABC Corporation//NONSGML My Product//EN\n" +
                 "BEGIN:VJOURNAL\n" +
                 "DTSTAMP:19970324T120000Z\n" +
                 "UID:uid5@example.com\n" +
                 "ORGANIZER:mailto:jsmith@example.com\n" +
                 "STATUS:DRAFT\n" +
                 "CLASS:PUBLIC\n" +
                 "CATEGORIES:Project Report,XYZ,Weekly Meeting\n" +
                 "DESCRIPTION:Project xyz Review Meeting Minutes\\n\n" +
                 " Agenda\\n1. Review of project version 1.0 requirements.\\n2.\n" +
                 "  Definition\n" +
                 " of project processes.\\n3. Review of project schedule.\\n\n" +
                 " Participants: John Smith\\, Jane Doe\\, Jim Dandy\\n-It was\n" +
                 "  decided that the requirements need to be signed off by\n" +
                 "  product marketing.\\n-Project processes were accepted.\\n\n" +
                 " -Project schedule needs to account for scheduled holidays\n" +
                 "  and employee vacation time. Check with HR for specific\n" +
                 "  dates.\\n-New schedule will be distributed by Friday.\\n-\n" +
                 " Next weeks meeting is cancelled. No meeting until 3/23.\n" +
                 "END:VJOURNAL\n" +
                 "END:VCALENDAR\n";
         ctx = getParser(source).icalobject();
        assertThat( ctx.getText().trim(), is(source.replace("\n ", "").trim()) );
         assertThat( ctx.calprop().size(), is(2) );
         assertThat( ctx.component().size(), is(1) );
 
         source = "BEGIN:VCALENDAR\n" +
                 "VERSION:2.0\n" +
                 "PRODID:-//RDU Software//NONSGML HandCal//EN\n" +
                 "BEGIN:VFREEBUSY\n" +
                 "ORGANIZER:mailto:jsmith@example.com\n" +
                 "DTSTART:19980313T141711Z\n" +
                 "DTEND:19980410T141711Z\n" +
                 "FREEBUSY:19980314T233000Z/19980315T003000Z\n" +
                 "FREEBUSY:19980316T153000Z/19980316T163000Z\n" +
                 "FREEBUSY:19980318T030000Z/19980318T040000Z\n" +
                 "URL:http://www.example.com/calendar/busytime/jsmith.ifb\n" +
                 "END:VFREEBUSY\n" +
                 "END:VCALENDAR\n";
         ctx = getParser(source).icalobject();
        assertThat( ctx.getText().trim(), is(source.replace("\n ", "").trim()) );
         assertThat( ctx.calprop().size(), is(2) );
         assertThat( ctx.component().size(), is(1) );
     }
 
     @Test
     public void altrepparamTest() {
 
         // 3.2.1
         // altrepparam
         //  : ALTREP '=' '"' uri '"'
         //  ;
 
         String paramName = "ALTREP";
         String value = "CID:part3.msg.970415T083000@example.com";
         String source = paramName + "=\"" + value + "\"";
 
         ICalendarParser.AltrepparamContext ctx = getParser(source).altrepparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_altrep().getText(), is(paramName) );
         assertThat( ctx.uri().getText(), is(value) );
     }
 
 
     @Test
     public void cnparamTest() {
 
         // 3.2.2
         // cnparam
         //  : CN '=' param_value
         //  ;
 
         String paramName = "CN";
         String value = "\"John Smith\"";
         String source = paramName + "=" + value;
 
         ICalendarParser.CnparamContext ctx = getParser(source).cnparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_cn().getText(), is(paramName) );
         assertThat( ctx.param_value().getText(), is(value) );
     }
 
     @Test
     public void cutypeparamTest() {
 
         // 3.2.3
         // cutypeparam
         //  : CUTYPE '=' ( INDIVIDUAL
         //               | GROUP
         //               | RESOURCE
         //               | ROOM
         //               | UNKNOWN
         //               | X_NAME
         //               | iana_token
         //               )
         //  ;
 
         String paramName = "CUTYPE";
         String value = "GROUP";
         String source = paramName + "=" + value;
 
         ICalendarParser.CutypeparamContext ctx = getParser(source).cutypeparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_cutype().getText(), is(paramName) );
         assertThat( ctx.k_group().getText(), is(value) );
 
         value = "INDIVIDUAL";
         source = paramName + "=" + value;
 
         ctx = getParser(source).cutypeparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_cutype().getText(), is(paramName) );
         assertThat( ctx.k_individual().getText(), is(value) );
     }
 
     @Test
     public void delfromparamTest() {
 
         // 3.2.4
         // delfromparam
         // : DELEGATED_FROM '=' '"' cal_address '"' (',' '"' cal_address '"')*
         // ;
 
         String paramName = "DELEGATED-FROM";
         String value1 = "mailto:jsmith@example.com";
         String value2 = "mailto:mu@boo.com";
         String source = paramName + "=\"" + value1 + "\",\"" + value2 + "\"";
 
         ICalendarParser.DelfromparamContext ctx = getParser(source).delfromparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_delegated_from().getText(), is(paramName) );
         assertThat( ctx.cal_address().size(), is(2) );
         assertThat( ctx.cal_address().get(0).getText(), is(value1) );
         assertThat( ctx.cal_address().get(1).getText(), is(value2) );
     }
 
     @Test
     public void deltoparamTest() {
 
         // 3.2.5
         // deltoparam
         // : DELEGATED_TO '=' '"' cal_address '"' (',' '"' cal_address '"')*
         // ;
 
         String paramName = "DELEGATED-TO";
         String value1 = "mailto:jsmith@example.com";
         String value2 = "mailto:mu@boo.com";
         String source = paramName + "=\"" + value1 + "\",\"" + value2 + "\"";
 
         ICalendarParser.DeltoparamContext ctx = getParser(source).deltoparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_delegated_to().getText(), is(paramName) );
         assertThat( ctx.cal_address().size(), is(2) );
         assertThat( ctx.cal_address().get(0).getText(), is(value1) );
         assertThat( ctx.cal_address().get(1).getText(), is(value2) );
     }
 
     @Test
     public void dirparamTest() {
 
         // 3.2.6
         // dirparam
         // : DIR '=' '"' uri '"'
         // ;
 
         String paramName = "DIR";
         String value = "ldap://example.com:6666/o=ABC%20Industries,c=US???(cn=Jim%20Dolittle)";
         String source = paramName + "=\"" + value + "\"";
 
         ICalendarParser.DirparamContext ctx = getParser(source).dirparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_dir().getText(), is(paramName) );
         assertThat( ctx.uri().getText(), is(value) );
     }
 
     @Test
     public void encodingparamTest() {
 
         // 3.2.7
         // encodingparam
         //  : ENCODING '=' ( '8' BIT
         //                 | BASE '6' '4'
         //                 )
         //  ;
 
         String paramName = "ENCODING";
         String value = "8BIT";
 
         String source = paramName + "=" + value;
 
         ICalendarParser.EncodingparamContext ctx = getParser(source).encodingparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_encoding().getText(), is(paramName) );
         assertThat( ctx.k_bit().getText(), is(value.replaceAll("\\d", "")) );
 
         value = "Base64";
 
         source = paramName + "=" + value;
 
         ctx = getParser(source).encodingparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_encoding().getText(), is(paramName) );
         assertThat( ctx.k_base().getText(), is(value.replaceAll("\\d", "")) );
     }
 
     @Test
     public void fmttypeparamTest() {
 
         // 3.2.8
         // fmttypeparam
         // : FMTTYPE '=' type_name '/' subtype_name
         // ;
 
         String paramName = "FMTTYPE";
         String value1 = "application";
         String value2 = "msword";
         String source = paramName + "=" + value1 + "/" + value2;
 
         ICalendarParser.FmttypeparamContext ctx = getParser(source).fmttypeparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_fmttype().getText(), is(paramName) );
         assertThat( ctx.type_name().getText(), is(value1) );
         assertThat( ctx.subtype_name().getText(), is(value2) );
     }
 
     @Test
     public void fbtypeparamTest() {
 
         // 3.2.9
         // fbtypeparam
         //  : FBTYPE '=' ( FREE
         //               | BUSY
         //               | BUSY_UNAVAILABLE
         //               | BUSY_TENTATIVE
         //               | X_NAME
         //               | iana_token
         //               )
         //  ;
 
         String paramName = "FBTYPE";
         String value = "BUSY-UNAVAILABLE";
         String source = paramName + "=" + value;
 
         ICalendarParser.FbtypeparamContext ctx = getParser(source).fbtypeparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_fbtype().getText(), is(paramName) );
         assertThat( ctx.k_busy_unavailable().getText(), is(value) );
 
         value = "FREE";
         source = paramName + "=" + value;
 
         ctx = getParser(source).fbtypeparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_fbtype().getText(), is(paramName) );
         assertThat( ctx.k_free().getText(), is(value) );
     }
 
     @Test
     public void languageparamTest() {
 
         // 3.2.10
         // languageparam
         //  : LANGUAGE '=' language
         //  ;
 
         String paramName = "LANGUAGE";
         String value = "en-US:Company Holiday Party";
         String source = paramName + "=" + value;
 
         ICalendarParser.LanguageparamContext ctx = getParser(source).languageparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_language().getText(), is(paramName) );
         assertThat( ctx.language().getText(), is(value) );
     }
 
     @Test
     public void memberparamTest() {
 
         // 3.2.11
         // memberparam
         //  : MEMBER '=' '"' cal_address '"' (',' '"' cal_address '"')*
         //  ;
 
         String paramName = "MEMBER";
         String value1 = "mailto:projectA@example.com";
         String value2 = "mailto:projectB@example.com";
         String source = paramName + "=\"" + value1 + "\",\"" + value2 + "\"";
 
         ICalendarParser.MemberparamContext ctx = getParser(source).memberparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_member().getText(), is(paramName) );
         assertThat( ctx.cal_address().size(), is(2) );
         assertThat( ctx.cal_address().get(0).getText(), is(value1) );
         assertThat( ctx.cal_address().get(1).getText(), is(value2) );
     }
 
     @Test
     public void partstatparamTest() {
 
         // 3.2.12
         // partstatparam
         //  : PARTSTAT '=' ( partstat_event
         //                 | partstat_todo
         //                 | partstat_jour
         //                 )
         //  ;
 
         String paramName = "PARTSTAT";
         String value = "DELEGATED";
         String source = paramName + "=" + value;
 
         ICalendarParser.PartstatparamContext ctx = getParser(source).partstatparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_partstat().getText(), is(paramName) );
         assertThat( ctx.partstat_event().getText(), is(value) );
     }
 
     @Test
     public void rangeparamTest() {
 
         // 3.2.13
         // rangeparam
         //  : RANGE '=' THISANDFUTURE
         //  ;
 
         String paramName = "RANGE";
         String value = "THISANDFUTURE";
         String source = paramName + "=" + value;
 
         ICalendarParser.RangeparamContext ctx = getParser(source).rangeparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_range().getText(), is(paramName) );
         assertThat( ctx.k_thisandfuture().getText(), is(value) );
     }
 
     @Test
     public void trigrelparamTest() {
 
         // 3.2.14
         // trigrelparam
         //  : RELATED '=' ( START
         //                | END
         //                )
         //  ;
 
         String paramName = "RELATED";
         String value = "END";
         String source = paramName + "=" + value;
 
         ICalendarParser.TrigrelparamContext ctx = getParser(source).trigrelparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_related().getText(), is(paramName) );
         assertThat( ctx.k_end().getText(), is(value) );
     }
 
     @Test
     public void reltypeparamTest() {
 
         // 3.2.15
         // reltypeparam
         //  : RELTYPE '=' ( PARENT
         //                | CHILD
         //                | SIBLING
         //                | iana_token
         //                | X_NAME
         //                )
         //  ;
 
         String paramName = "RELTYPE";
         String value = "x-some-name";
         String source = paramName + "=" + value;
 
         ICalendarParser.ReltypeparamContext ctx = getParser(source).reltypeparam();
 
         assertThat( ctx.getText(), is(source) );
         assertThat( ctx.k_reltype().getText(), is(paramName) );
         assertThat( ctx.x_name().getText(), is(value) );
     }
 
     @Test
     public void roleparamTest() {
 
         // 3.2.16
         // roleparam
         //  : ROLE '=' ( CHAIR
         //             | REQ_PARTICIPANT
         //             | OPT_PARTICIPANT
         //             | NON_PARTICIPANT
         //             | iana_token
         //             | X_NAME
         //             )
         //  ;
 
         String paramName = "ROLE";
         String value = "iana-some-name";
         String source = paramName + "=" + value;
 
         ICalendarParser.RoleparamContext ctx = getParser(source).roleparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_role().getText(), is(paramName) );
         assertThat( ctx.iana_token().getText(), is(value) );
     }
 
     @Test
     public void rsvpparamTest() {
 
         // 3.2.17
         // rsvpparam
         //  : RSVP '=' ( TRUE
         //             | FALSE
         //             )
         //  ;
 
 
         String paramName = "RSVP";
         String value = "TRUE";
         String source = paramName + "=" + value;
 
         ICalendarParser.RsvpparamContext ctx = getParser(source).rsvpparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_rsvp().getText(), is(paramName) );
         assertThat( ctx.k_true().getText(), is(value) );
     }
 
     @Test
     public void sentbyparamTest() {
 
         // 3.2.18
         // sentbyparam
         //  : SENT_BY '=' '"' cal_address '"'
         //  ;
 
         String paramName = "SENT-BY";
         String value = "mailto:projectA@example.com";
         String source = paramName + "=\"" + value + "\"";
 
         ICalendarParser.SentbyparamContext ctx = getParser(source).sentbyparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_sent_by().getText(), is(paramName) );
         assertThat( ctx.cal_address().getText(), is(value) );
     }
 
     @Test
     public void tzidparamTest() {
 
         // 3.2.19
         // tzidparam
         //  : TZID '=' '/'? paramtext
         //  ;
 
         String paramName = "TZID";
         String value = "America/New_York";
         String source = paramName + "=/" + value;
 
         ICalendarParser.TzidparamContext ctx = getParser(source).tzidparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_tzid().getText(), is(paramName) );
         assertThat( ctx.paramtext().getText(), is(value) );
     }
 
     @Test
     public void valuetypeparamTest() {
 
         // 3.2.20
         // valuetypeparam
         //  : VALUE '=' valuetype
         //  ;
 
         String paramName = "VALUE";
         String value = "TIME";
         String source = paramName + "=" + value;
 
         ICalendarParser.ValuetypeparamContext ctx = getParser(source).valuetypeparam();
 
         assertThat( ctx.getText(), is(source) );
 
         assertThat( ctx.k_value().getText(), is(paramName) );
         assertThat( ctx.valuetype().getText(), is(value) );
     }
 
     @Test
     public void binaryTest() {
 
         // 3.3.1 - A "BASE64" encoded character string, as defined by [RFC4648].
         // binary
         //  : b_chars b_end?
         //  ;
 
         String value = "AAABAAEAEBAQAAEABAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAA\n" +
                 " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAAAgIAAAICAgADAwMAA////AAAA\n" +
                 " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
                 " AAAAAAAAAAAAAAAAAAAAAAMwAAAAAAABNEMQAAAAAAAkQgAAAAAAJEREQgAA\n" +
                 " ACECQ0QgEgAAQxQzM0E0AABERCRCREQAADRDJEJEQwAAAhA0QwEQAAAAAERE\n" +
                 " AAAAAAAAREQAAAAAAAAkQgAAAAAAAAMgAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
                 " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
                 " AAAAAAAAAA";
 
         String end = "==";
 
         String source = value + end;
 
         ICalendarParser.BinaryContext ctx = getParser(source).binary();
 
         assertThat( ctx.b_chars().getText(), is(value.replace("\n ", "")) );
         assertThat( ctx.b_end().getText(), is(end) );
     }
 
     @Test
     public void boolTest() {
 
         // 3.3.2
         // bool
         //  : TRUE
         //  | FALSE
         //  ;
 
         String source = "trUe";
         ICalendarParser.BoolContext ctx = getParser(source).bool();
         assertThat( ctx.k_true().getText(), is(source) );
     }
 
     @Test
     public void cal_addressTest() {
 
         // 3.3.3
         // cal_address
         //  : uri
         //  ;
 
         String source = "mailto:jane_doe@example.com";
         ICalendarParser.Cal_addressContext ctx = getParser(source).cal_address();
         assertThat( ctx.uri().getText(), is(source) );
     }
 
     @Test
     public void dateTest() {
 
         // 3.3.4
         // date
         //  : date_value
         //  ;
 
         String source = "19970714";
         ICalendarParser.DateContext ctx = getParser(source).date();
         assertThat( ctx.date_value().getText(), is(source) );
     }
 
     @Test
     public void date_timeTest() {
 
         // 3.3.5
         // date_time
         //  : date K_T time
         //  ;
 
         String date = "19980118";
         String time = "230000";
         String source = date + "t" + time;
         ICalendarParser.Date_timeContext ctx = getParser(source).date_time();
         assertThat( ctx.date().getText(), is(date) );
         assertThat( ctx.time().getText(), is(time) );
     }
 
     @Test
     public void dur_valueTest() {
 
         // 3.3.6
         // dur_value
         //  : '-' (K_P | NON_KEYWORD) (dur_date | dur_time | dur_week)
         //  | '+'? (K_P  | NON_KEYWORD) (dur_date | dur_time | dur_week)
         //  ;
 
         String date = "15DT5H0M20S";
         String source = "+P" + date;
         ICalendarParser.Dur_valueContext ctx = getParser(source).dur_value();
         assertThat( ctx.dur_date().getText(), is(date) );
 
         String weeks = "7w";
         source = "P" + weeks;
         ctx = getParser(source).dur_value();
         assertThat( ctx.dur_week().getText(), is(weeks) );
     }
 
     @Test
     public void float_numTest() {
 
         // 3.3.7
         // float_num
         //  : '-' digits ('.' digits)?
         //  | '+'? digits ('.' digits)?
         //  ;
 
         String source = "123.45";
         ICalendarParser.Float_numContext ctx = getParser(source).float_num();
         assertThat( ctx.digits().get(0).getText(), is("123") );
         assertThat( ctx.digits().get(1).getText(), is("45") );
 
         source = "-42";
         ctx = getParser(source).float_num();
         assertThat( ctx.digits().get(0).getText(), is("42") );
     }
 
     @Test
     public void integerTest() {
 
         // 3.3.8
         // integer
         //  : '-' digits
         //  | '+'? digits
         //  ;
 
         String source = "+123";
         ICalendarParser.IntegerContext ctx = getParser(source).integer();
         assertThat( ctx.digits().getText(), is("123") );
     }
 
     @Test
     public void periodTest() {
 
         // 3.3.9
         // period
         //  : period_explicit
         //  | period_start
         //  ;
 
         String source = "19970101T180000Z/19970102T070000Z";
         ICalendarParser.PeriodContext ctx = getParser(source).period();
         assertThat( ctx.period_explicit().getText(), is(source) );
 
         source = "19970101T180000Z/PT5H30M";
         ctx = getParser(source).period();
         assertThat( ctx.period_start().getText(), is(source) );
     }
 
     @Test
     public void recurTest() {
 
         // 3.3.10
         // recur
         //  : recur_rule_part (';' recur_rule_part)*
         //  ;
 
         String value1 = "FREQ=DAILY";
         String value2 = "BYDAY=-42WE";
         String source = value1 + ";" + value2;
 
         ICalendarParser.RecurContext ctx = getParser(source).recur();
 
         assertThat( ctx.recur_rule_part().size(), is(2) );
 
         assertThat( ctx.recur_rule_part().get(0).getText(), is(value1) );
         assertThat( ctx.recur_rule_part().get(1).getText(), is(value2) );
     }
 
     @Test
     public void textTest() {
 
         // 3.3.11
         // text
         //  : (tsafe_char | ':' | '"' | escaped_char)*
         //  ;
 
         String source = "Project XYZ Final Review\\nConference Room - 3B\\nCome Prepared.";
         ICalendarParser.TextContext ctx = getParser(source).text();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void timeTest() {
 
         // 3.3.12
         // time
         //  : time_hour time_minute time_second K_Z?
         //  ;
 
         String hour = "12";
         String minute = "34";
         String second = "56";
         String source = hour + minute + second;
 
         ICalendarParser.TimeContext ctx = getParser(source).time();
 
         assertThat( ctx.time_hour().getText(), is(hour) );
         assertThat( ctx.time_minute().getText(), is(minute) );
         assertThat( ctx.time_second().getText(), is(second) );
 
         hour = "23";
         minute = "00";
         second = "09";
         source = hour + minute + second + "Z";
 
         ctx = getParser(source).time();
 
         assertThat( ctx.time_hour().getText(), is(hour) );
         assertThat( ctx.time_minute().getText(), is(minute) );
         assertThat( ctx.time_second().getText(), is(second) );
     }
 
     @Test
     public void uriTest() {
 
         // 3.3.13 - As defined in Section 3 of [RFC3986].
         // uri
         //  : qsafe_char+
         //  ;
 
         String source = "http://example.com/my-report.txt";
         ICalendarParser.UriContext ctx = getParser(source).uri();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void utc_offsetTest() {
 
         // 3.3.14
         // utc_offset
         //  : time_numzone
         //  ;
 
         String hour = "12";
         String minute = "34";
         String second = "";
         String source = "+" + hour + minute + second;
 
         ICalendarParser.Utc_offsetContext ctx = getParser(source).utc_offset();
 
         assertThat( ctx.getText().startsWith("+"), is(true) );
         assertThat( ctx.time_numzone().time_hour().getText(), is(hour) );
         assertThat( ctx.time_numzone().time_minute().getText(), is(minute) );
 
         hour = "23";
         minute = "00";
         second = "09";
         source = "-" + hour + minute + second;
 
         ctx = getParser(source).utc_offset();
 
         assertThat( ctx.getText().startsWith("-"), is(true) );
         assertThat( ctx.time_numzone().time_hour().getText(), is(hour) );
         assertThat( ctx.time_numzone().time_minute().getText(), is(minute) );
         assertThat( ctx.time_numzone().time_second().getText(), is(second) );
     }
 
     @Test
     public void eventcTest() {
 
         // 3.6.1 - Event Component
         // eventc
         //  : BEGIN ':' VEVENT CRLF
         //    eventprop*
         //    alarmc*
         //    END ':' VEVENT CRLF
         //  ;
 
         String source = "BEGIN:VEVENT\n" +
                 "UID:19970901T130000Z-123401@example.com\n" +
                 "DTSTAMP:19970901T130000Z\n" +
                 "DTSTART:19970903T163000Z\n" +
                 "DTEND:19970903T190000Z\n" +
                 "SUMMARY:Annual Employee Review\n" +
                 "CLASS:PRIVATE\n" +
                 "CATEGORIES:BUSINESS,HUMAN RESOURCES\n" +
                 "END:VEVENT\n";
 
         ICalendarParser.EventcContext ctx = getParser(source).eventc();
         assertThat( ctx.eventprop().size(), is(7) );
 
         source = "BEGIN:VEVENT\n" +
                 "UID:19970901T130000Z-123402@example.com\n" +
                 "DTSTAMP:19970901T130000Z\n" +
                 "DTSTART:19970401T163000Z\n" +
                 "DTEND:19970402T010000Z\n" +
                 "SUMMARY:Laurel is in sensitivity awareness class.\n" +
                 "CLASS:PUBLIC\n" +
                 "CATEGORIES:BUSINESS,HUMAN RESOURCES\n" +
                 "TRANSP:TRANSPARENT\n" +
                 "END:VEVENT\n";
 
         ctx = getParser(source).eventc();
         assertThat( ctx.eventprop().size(), is(8) );
 
         source = "BEGIN:VEVENT\n" +
                 "UID:19970901T130000Z-123403@example.com\n" +
                 "DTSTAMP:19970901T130000Z\n" +
                 "DTSTART;VALUE=DATE:19971102\n" +
                 "SUMMARY:Our Blissful Anniversary\n" +
                 "TRANSP:TRANSPARENT\n" +
                 "CLASS:CONFIDENTIAL\n" +
                 "CATEGORIES:ANNIVERSARY,PERSONAL,SPECIAL OCCASION\n" +
                 "RRULE:FREQ=YEARLY\n" +
                 "END:VEVENT\n";
 
         ctx = getParser(source).eventc();
         assertThat( ctx.eventprop().size(), is(8) );
 
         source = "BEGIN:VEVENT\n" +
                 "UID:20070423T123432Z-541111@example.com\n" +
                 "DTSTAMP:20070423T123432Z\n" +
                 "DTSTART;VALUE=DATE:20070628\n" +
                 "DTEND;VALUE=DATE:20070709\n" +
                 "SUMMARY:Festival International de Jazz de Montreal\n" +
                 "TRANSP:TRANSPARENT\n" +
                 "END:VEVENT\n";
 
         ctx = getParser(source).eventc();
         assertThat( ctx.eventprop().size(), is(6) );
     }
 
     @Test
     public void todocTest() {
 
         // 3.6.2 - To-Do Component
         // todoc
         //  : BEGIN ':' VTODO CRLF
         //    todoprop*
         //    alarmc*
         //    END ':' VTODO CRLF
         //  ;
 
         String source = "BEGIN:VTODO\n" +
                 "UID:20070313T123432Z-456553@example.com\n" +
                 "DTSTAMP:20070313T123432Z\n" +
                 "DUE;VALUE=DATE:20070501\n" +
                 "SUMMARY:Submit Quebec Income Tax Return for 2006\n" +
                 "CLASS:CONFIDENTIAL\n" +
                 "CATEGORIES:FAMILY,FINANCE\n" +
                 "STATUS:NEEDS-ACTION\n" +
                 "END:VTODO\n";
 
         ICalendarParser.TodocContext ctx = getParser(source).todoc();
         assertThat( ctx.todoprop().size(), is(7) );
 
         source = "BEGIN:VTODO\n" +
                 "UID:20070514T103211Z-123404@example.com\n" +
                 "DTSTAMP:20070514T103211Z\n" +
                 "DTSTART:20070514T110000Z\n" +
                 "DUE:20070709T130000Z\n" +
                 "COMPLETED:20070707T100000Z\n" +
                 "SUMMARY:Submit Revised Internet-Draft\n" +
                 "PRIORITY:1\n" +
                 "STATUS:NEEDS-ACTION\n" +
                 "END:VTODO\n";
 
         ctx = getParser(source).todoc();
         assertThat( ctx.todoprop().size(), is(8) );
     }
 
     @Test
     public void journalcTest() {
 
         // 3.6.3 - Journal Component
         // journalc
         //  : BEGIN ':' VJOURNAL CRLF
         //    jourprop*
         //    END ':' VJOURNAL CRLF
         //  ;
 
         String source = "BEGIN:VJOURNAL\n" +
                 "UID:19970901T130000Z-123405@example.com\n" +
                 "DTSTAMP:19970901T130000Z\n" +
                 "DTSTART;VALUE=DATE:19970317\n" +
                 "SUMMARY:Staff meeting minutes\n" +
                 "DESCRIPTION:1. Staff meeting: Participants include Joe\\,\n" +
                 "  Lisa\\, and Bob. Aurora project plans were reviewed.\n" +
                 "  There is currently no budget reserves for this project.\n" +
                 "  Lisa will escalate to management. Next meeting on Tuesday.\\n\n" +
                 " 2. Telephone Conference: ABC Corp. sales representative\n" +
                 "  called to discuss new printer. Promised to get us a demo by\n" +
                 "  Friday.\\n3. Henry Miller (Handsoff Insurance): Car was\n" +
                 "  totaled by tree. Is looking into a loaner car. 555-2323\n" +
                 "  (tel).\n" +
                 "END:VJOURNAL\n";
 
         ICalendarParser.JournalcContext ctx = getParser(source).journalc();
         assertThat( ctx.jourprop().size(), is(5) );
     }
 
     @Test
     public void freebusycTest() {
 
         // 3.6.4 - Free/Busy Component
         // freebusyc
         //  : BEGIN ':' VFREEBUSY CRLF
         //    fbprop*
         //    END ':' VFREEBUSY CRLF
         //  ;
 
         String source = "BEGIN:VFREEBUSY\n" +
                 "UID:19970901T082949Z-FA43EF@example.com\n" +
                 "ORGANIZER:mailto:jane_doe@example.com\n" +
                 "ATTENDEE:mailto:john_public@example.com\n" +
                 "DTSTART:19971015T050000Z\n" +
                 "DTEND:19971016T050000Z\n" +
                 "DTSTAMP:19970901T083000Z\n" +
                 "END:VFREEBUSY\n";
 
         ICalendarParser.FreebusycContext ctx = getParser(source).freebusyc();
         assertThat( ctx.fbprop().size(), is(6) );
 
         source = "BEGIN:VFREEBUSY\n" +
                 "UID:19970901T095957Z-76A912@example.com\n" +
                 "ORGANIZER:mailto:jane_doe@example.com\n" +
                 "ATTENDEE:mailto:john_public@example.com\n" +
                 "DTSTAMP:19970901T100000Z\n" +
                 "FREEBUSY:19971015T050000Z/PT8H30M,\n" +
                 " 19971015T160000Z/PT5H30M,19971015T223000Z/PT6H30M\n" +
                 "URL:http://example.com/pub/busy/jpublic-01.ifb\n" +
                 "COMMENT:This iCalendar file contains busy time information for\n" +
                 "  the next three months.\n" +
                 "END:VFREEBUSY\n";
 
         ctx = getParser(source).freebusyc();
         assertThat( ctx.fbprop().size(), is(7) );
 
         source = "BEGIN:VFREEBUSY\n" +
                 "UID:19970901T115957Z-76A912@example.com\n" +
                 "DTSTAMP:19970901T120000Z\n" +
                 "ORGANIZER:jsmith@example.com\n" +
                 "DTSTART:19980313T141711Z\n" +
                 "DTEND:19980410T141711Z\n" +
                 "FREEBUSY:19980314T233000Z/19980315T003000Z\n" +
                 "FREEBUSY:19980316T153000Z/19980316T163000Z\n" +
                 "FREEBUSY:19980318T030000Z/19980318T040000Z\n" +
                 "URL:http://www.example.com/calendar/busytime/jsmith.ifb\n" +
                 "END:VFREEBUSY\n";
 
         ctx = getParser(source).freebusyc();
         assertThat( ctx.fbprop().size(), is(9) );
     }
 
     @Test
     public void timezonecTest() {
 
         // 3.6.5 - Time Zone Component
         // timezonec
         //  : BEGIN ':' VTIMEZONE CRLF
         //    timezoneprop*
         //    END ':' VTIMEZONE CRLF
         //  ;
 
         String source = "BEGIN:VTIMEZONE\n" +
                 "TZID:America/New_York\n" + // property 1
                 "LAST-MODIFIED:20050809T050000Z\n" + // property 2
                 "BEGIN:DAYLIGHT\n" + // property 3
                 "DTSTART:19670430T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=4;BYDAY=-1SU;UNTIL=19730429T070000Z\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "BEGIN:STANDARD\n" + // property 4
                 "DTSTART:19671029T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU;UNTIL=20061029T060000Z\n" +
                 "TZOFFSETFROM:-0400\n" +
                 "TZOFFSETTO:-0500\n" +
                 "TZNAME:EST\n" +
                 "END:STANDARD\n" +
                 "BEGIN:DAYLIGHT\n" + // property 5
                 "DTSTART:19740106T020000\n" +
                 "RDATE:19750223T020000\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "BEGIN:DAYLIGHT\n" + // property 6
                 "DTSTART:19760425T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=4;BYDAY=-1SU;UNTIL=19860427T070000Z\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "BEGIN:DAYLIGHT\n" + // property 7
                 "DTSTART:19870405T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=4;BYDAY=1SU;UNTIL=20060402T070000Z\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "BEGIN:DAYLIGHT\n" + // property 8
                 "DTSTART:20070311T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "BEGIN:STANDARD\n" + // property 9
                 "DTSTART:20071104T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\n" +
                 "TZOFFSETFROM:-0400\n" +
                 "TZOFFSETTO:-0500\n" +
                 "TZNAME:EST\n" +
                 "END:STANDARD\n" +
                 "END:VTIMEZONE\n";
 
         ICalendarParser.TimezonecContext ctx = getParser(source).timezonec();
         assertThat( ctx.timezoneprop().size(), is(9) );
 
         source = "BEGIN:VTIMEZONE\n" +
                 "TZID:America/New_York\n" + // property 1
                 "LAST-MODIFIED:20050809T050000Z\n" + // property 2
                 "BEGIN:STANDARD\n" + // property 3
                 "DTSTART:20071104T020000\n" +
                 "TZOFFSETFROM:-0400\n" +
                 "TZOFFSETTO:-0500\n" +
                 "TZNAME:EST\n" +
                 "END:STANDARD\n" +
                 "BEGIN:DAYLIGHT\n" + // property 4
                 "DTSTART:20070311T020000\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "END:VTIMEZONE\n";
 
         ctx = getParser(source).timezonec();
         assertThat( ctx.timezoneprop().size(), is(4) );
 
         source = "BEGIN:VTIMEZONE\n" +
                 "TZID:America/New_York\n" + // property 1
                 "LAST-MODIFIED:20050809T050000Z\n" + // property 2
                 "TZURL:http://zones.example.com/tz/America-New_York.ics\n" + // property 3
                 "BEGIN:STANDARD\n" + // property 4
                 "DTSTART:20071104T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\n" +
                 "TZOFFSETFROM:-0400\n" +
                 "TZOFFSETTO:-0500\n" +
                 "TZNAME:EST\n" +
                 "END:STANDARD\n" +
                 "BEGIN:DAYLIGHT\n" + // property 5
                 "DTSTART:20070311T020000\n" +
                 "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "END:VTIMEZONE\n";
 
         ctx = getParser(source).timezonec();
         assertThat( ctx.timezoneprop().size(), is(5) );
 
         source = "BEGIN:VTIMEZONE\n" +
                 "TZID:Fictitious\n" + // property 1
                 "LAST-MODIFIED:19870101T000000Z\n" + // property 2
                 "BEGIN:STANDARD\n" + // property 3
                 "DTSTART:19671029T020000\n" +
                 "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\n" +
                 "TZOFFSETFROM:-0400\n" +
                 "TZOFFSETTO:-0500\n" +
                 "TZNAME:EST\n" +
                 "END:STANDARD\n" +
                 "BEGIN:DAYLIGHT\n" + // property 4
                 "DTSTART:19870405T020000\n" +
                 "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4;UNTIL=19980404T070000Z\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "END:VTIMEZONE\n";
 
         ctx = getParser(source).timezonec();
         assertThat( ctx.timezoneprop().size(), is(4) );
 
         source = "BEGIN:VTIMEZONE\n" +
                 "TZID:Fictitious\n" + // property 1
                 "LAST-MODIFIED:19870101T000000Z\n" + // property 2
                 "BEGIN:STANDARD\n" + // property 3
                 "DTSTART:19671029T020000\n" +
                 "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\n" +
                 "TZOFFSETFROM:-0400\n" +
                 "TZOFFSETTO:-0500\n" +
                 "TZNAME:EST\n" +
                 "END:STANDARD\n" +
                 "BEGIN:DAYLIGHT\n" + // property 4
                 "DTSTART:19870405T020000\n" +
                 "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4;UNTIL=19980404T070000Z\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "BEGIN:DAYLIGHT\n" + // property 5
                 "DTSTART:19990424T020000\n" +
                 "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=4\n" +
                 "TZOFFSETFROM:-0500\n" +
                 "TZOFFSETTO:-0400\n" +
                 "TZNAME:EDT\n" +
                 "END:DAYLIGHT\n" +
                 "END:VTIMEZONE\n";
 
         ctx = getParser(source).timezonec();
         assertThat( ctx.timezoneprop().size(), is(5) );
     }
 
     @Test
     public void alarmcTest() {
 
         // 3.6.6 - Alarm Component
         // alarmc
         //  : BEGIN ':' VALARM CRLF
         //    alarmprop?
         //    END ':' VALARM CRLF
         //  ;
 
         String source = "BEGIN:VALARM\n" +
                 "TRIGGER;VALUE=DATE-TIME:19970317T133000Z\n" +
                 "REPEAT:4\n" +
                 "DURATION:PT15M\n" +
                 "ACTION:AUDIO\n" +
                 "ATTACH;FMTTYPE=audio/basic:ftp://example.com/pub/\n" +
                 " sounds/bell-01.aud\n" +
                 "END:VALARM\n";
 
         ICalendarParser.AlarmcContext ctx = getParser(source).alarmc();
         assertThat( ctx.alarmprop().size(), is(5) );
 
         source = "BEGIN:VALARM\n" +
                 "TRIGGER:-PT30M\n" +
                 "REPEAT:2\n" +
                 "DURATION:PT15M\n" +
                 "ACTION:DISPLAY\n" +
                 "DESCRIPTION:Breakfast meeting with executive\\n\n" +
                 " team at 8:30 AM EST.\n" +
                 "END:VALARM\n";
 
         ctx = getParser(source).alarmc();
         assertThat( ctx.alarmprop().size(), is(5) );
 
         source = "BEGIN:VALARM\n" +
                 "TRIGGER;RELATED=END:-P2D\n" +
                 "ACTION:EMAIL\n" +
                 "ATTENDEE:mailto:john_doe@example.com\n" +
                 "SUMMARY:*** REMINDER: SEND AGENDA FOR WEEKLY STAFF MEETING ***\n" +
                 "DESCRIPTION:A draft agenda needs to be sent out to the attendees\n" +
                 "  to the weekly managers meeting (MGR-LIST). Attached is a\n" +
                 "  pointer the document template for the agenda file.\n" +
                 "ATTACH;FMTTYPE=application/msword:http://example.com/\n" +
                 " templates/agenda.doc\n" +
                 "END:VALARM\n";
 
         ctx = getParser(source).alarmc();
         assertThat( ctx.alarmprop().size(), is(6) );
     }
 
     @Test
     public void calscaleTest() {
 
         // 3.7.1 - Calendar Scale
         // calscale
         //  : CALSCALE (';' other_param)* ':' GREGORIAN CRLF
         //  ;
 
         String source = "CALSCALE:GREGORIAN\n";
 
         ICalendarParser.CalscaleContext ctx = getParser(source).calscale();
         assertThat(ctx.k_calscale().getText(), is("CALSCALE"));
         assertThat( ctx.k_gregorian().getText(), is("GREGORIAN") );
     }
 
     @Test
     public void methodTest() {
 
         // 3.7.2 - Method
         // method
         //  : METHOD (';' other_param)* ':' iana_token CRLF
         //  ;
 
         String source = "METHOD:REQUEST\n";
 
         ICalendarParser.MethodContext ctx = getParser(source).method();
         assertThat( ctx.k_method().getText(), is("METHOD") );
         assertThat( ctx.iana_token().getText(), is("REQUEST") );
     }
 
     @Test
     public void prodidTest() {
 
         // 3.7.3 - Product Identifier
         // prodid
         //  : PRODID (';' other_param)* ':' text CRLF
         //  ;
 
         String source = "PRODID:-//ABC Corporation//NONSGML My Product//EN\n";
 
         ICalendarParser.ProdidContext ctx = getParser(source).prodid();
         assertThat( ctx.k_prodid().getText(), is("PRODID") );
         assertThat( ctx.text().getText(), is("-//ABC Corporation//NONSGML My Product//EN") );
     }
 
     @Test
     public void versionTest() {
 
         // 3.7.4 - Version
         // version
         //  : VERSION (';' other_param)* ':' vervalue CRLF
         //  ;
 
         String source = "VERSION:2.0\n";
 
         ICalendarParser.VersionContext ctx = getParser(source).version();
         assertThat( ctx.k_version().getText(), is("VERSION") );
         assertThat( ctx.vervalue().float_num(0).getText(), is("2.0") );
 
         source = "VERSION:1.1;2.2\n";
 
         ctx = getParser(source).version();
         assertThat( ctx.k_version().getText(), is("VERSION") );
         assertThat( ctx.vervalue().float_num(0).getText(), is("1.1") );
         assertThat( ctx.vervalue().float_num(1).getText(), is("2.2") );
     }
 
     @Test
     public void attachTest() {
 
         // 3.8.1.1 - Attachment
         // attach
         //  : ATTACH attachparam* ( ':' uri
         //                        | ';' ENCODING '=' BASE '6' '4' ';' VALUE '=' BINARY ':' binary
         //                        )
         //    CRLF
         //  ;
 
         String source = "ATTACH:CID:jsmith.part3.960817T083000.xyzMail@example.com\n";
 
         ICalendarParser.AttachContext ctx = getParser(source).attach();
         assertThat( ctx.k_attach().getText(), is("ATTACH") );
         assertThat( ctx.uri().getText(), is("CID:jsmith.part3.960817T083000.xyzMail@example.com") );
 
         source = "ATTACH;FMTTYPE=application/postscript:ftp://example.com/pub/\n" +
                 " reports/r-960812.ps\n";
 
         ctx = getParser(source).attach();
         assertThat( ctx.k_attach().getText(), is("ATTACH") );
         assertThat( ctx.attachparam().size(), is(1) );
         assertThat( ctx.attachparam(0).fmttypeparam().getText(), is("FMTTYPE=application/postscript") );
         assertThat( ctx.uri().getText(), is("ftp://example.com/pub/reports/r-960812.ps") );
     }
 
     @Test
     public void categoriesTest() {
 
         // 3.8.1.2 - Categories
         // categories
         //  : CATEGORIES catparam* ':' text (',' text)* CRLF
         //  ;
 
         String source = "CATEGORIES:MEETING\n";
 
         ICalendarParser.CategoriesContext ctx = getParser(source).categories();
         assertThat( ctx.k_categories().getText(), is("CATEGORIES") );
         assertThat( ctx.text().size(), is(1) );
         assertThat( ctx.text(0).getText(), is("MEETING") );
 
         source = "CATEGORIES:APPOINTMENT,EDUCATION\n";
 
         ctx = getParser(source).categories();
         assertThat( ctx.k_categories().getText(), is("CATEGORIES") );
         assertThat( ctx.text().size(), is(2) );
         assertThat( ctx.text(0).getText(), is("APPOINTMENT") );
         assertThat( ctx.text(1).getText(), is("EDUCATION") );
     }
 
     @Test
     public void clazzTest() {
 
         // 3.8.1.3 - Classification
         // clazz
         //  : CLASS (';' other_param)* ':' classvalue CRLF
         //  ;
 
         String source = "CLASS:PUBLIC\n";
 
         ICalendarParser.ClazzContext ctx = getParser(source).clazz();
         assertThat( ctx.k_class().getText(), is("CLASS") );
         assertThat( ctx.classvalue().getText(), is("PUBLIC") );
     }
 
     @Test
     public void commentTest() {
 
         // 3.8.1.4 - Comment
         // comment
         //  : COMMENT commparam* ':' text CRLF
         //  ;
 
         String comment = "The meeting really needs to include both ourselves\n" +
                 " and the customer. We can't hold this meeting without them.\n" +
                 " As a matter of fact\\, the venue for the meeting ought to be at\n" +
                 " their site. - - John";
 
         String source = "COMMENT:" + comment + "\n";
 
         ICalendarParser.CommentContext ctx = getParser(source).comment();
         assertThat( ctx.k_comment().getText(), is("COMMENT") );
         assertThat( ctx.text().getText(), is(comment.replace("\n ", "")) );
     }
 
     @Test
     public void descriptionTest() {
 
         // 3.8.1.5 - Description
         // description
         //  : DESCRIPTION descparam* ':' text CRLF
         //  ;
 
         String description = "Meeting to provide technical review for \"Phoenix\"\n" +
                 " design.\\nHappy Face Conference Room. Phoenix design team\n" +
                 " MUST attend this meeting.\\nRSVP to team leader.";
 
         String source = "DESCRIPTION:" + description + "\n";
 
         ICalendarParser.DescriptionContext ctx = getParser(source).description();
 
         assertThat( ctx.k_description().getText(), is("DESCRIPTION") );
         assertThat( ctx.text().getText(), is(description.replace("\n ", "")) );
     }
 
     @Test
     public void geoTest() {
 
         // 3.8.1.6 - Geographic Position
         // geo
         //  : GEO (';' other_param)* ':' geovalue CRLF
         //  ;
 
         String source = "GEO:37.386013;-122.082932\n";
 
         ICalendarParser.GeoContext ctx = getParser(source).geo();
 
         assertThat( ctx.k_geo().getText(), is("GEO") );
 
         assertThat( ctx.geovalue().float_num().size(), is(2) );
 
         assertThat( ctx.geovalue().float_num(0).getText(), is("37.386013") );
         assertThat( ctx.geovalue().float_num(1).getText(), is("-122.082932") );
     }
 
     @Test
     public void locationTest() {
 
         // 3.8.1.7 - Location
         // location
         //  : LOCATION locparam* ':' text CRLF
         //  ;
 
         String source = "LOCATION:Conference Room - F123\\, Bldg. 002\n";
 
         ICalendarParser.LocationContext ctx = getParser(source).location();
 
         assertThat( ctx.k_location().getText(), is("LOCATION") );
         assertThat( ctx.text().getText(), is("Conference Room - F123\\, Bldg. 002") );
 
         source = "LOCATION;ALTREP=\"http://xyzcorp.com/conf-rooms/f123.vcf\":\n" +
                 " Conference Room - F123\\, Bldg. 002\n";
 
         ctx = getParser(source).location();
 
         assertThat( ctx.k_location().getText(), is("LOCATION") );
         assertThat( ctx.locparam().size(), is(1) );
         assertThat( ctx.locparam(0).altrepparam().uri().getText(), is("http://xyzcorp.com/conf-rooms/f123.vcf") );
         assertThat( ctx.text().getText(), is("Conference Room - F123\\, Bldg. 002") );
 
         source = "LOCATION:Sabai Thai\\, 165-169 Princes House\\, Princes Place\\, North Street\\\n" +
                 " , Brighton. BN1 1EA\n";
 
         ctx = getParser(source).location();
 
         assertThat( ctx.k_location().getText(), is("LOCATION") );
         assertThat( ctx.locparam().size(), is(0) );
         assertThat( ctx.text().getText(), is("Sabai Thai\\, 165-169 Princes House\\, Princes Place\\, North Street\\\n" +
                 " , Brighton. BN1 1EA") );
     }
 
     @Test
     public void percentTest() {
 
         // 3.8.1.8 - Percent Complete
         // percent
         //  : PERCENT_COMPLETE (';' other_param)* ':' integer CRLF
         //  ;
 
         String source = "PERCENT-COMPLETE:39\n";
 
         ICalendarParser.PercentContext ctx = getParser(source).percent();
 
         assertThat( ctx.k_percent_complete().getText(), is("PERCENT-COMPLETE") );
         assertThat( ctx.integer().getText(), is("39") );
     }
 
     @Test
     public void priorityTest() {
 
         // 3.8.1.9 - Priority
         // priority
         //  : PRIORITY (';' other_param)* ':' priovalue CRLF
         //  ;
 
         String source = "PRIORITY:1\n";
 
         ICalendarParser.PriorityContext ctx = getParser(source).priority();
 
         assertThat( ctx.k_priority().getText(), is("PRIORITY") );
         assertThat( ctx.priovalue().getText(), is("1") );
     }
 
     @Test
     public void resourcesTest() {
 
         // 3.8.1.10 - Resources
         // resources
         //  : RESOURCES resrcparam* ':' text (',' text)* CRLF
         //  ;
 
         String source = "RESOURCES:EASEL,PROJECTOR,VCR\n";
 
         ICalendarParser.ResourcesContext ctx = getParser(source).resources();
 
         assertThat( ctx.k_resources().getText(), is("RESOURCES") );
         assertThat( ctx.text().size(), is(3) );
         assertThat( ctx.text(0).getText(), is("EASEL") );
         assertThat( ctx.text(1).getText(), is("PROJECTOR") );
         assertThat( ctx.text(2).getText(), is("VCR") );
 
         source = "RESOURCES;LANGUAGE=fr:Nettoyeur haute pression\n";
 
         ctx = getParser(source).resources();
 
         assertThat( ctx.k_resources().getText(), is("RESOURCES") );
         assertThat( ctx.resrcparam().size(), is(1) );
         assertThat( ctx.resrcparam(0).languageparam().language().getText(), is("fr") );
         assertThat( ctx.text().size(), is(1) );
         assertThat( ctx.text(0).getText(), is("Nettoyeur haute pression") );
     }
 
     @Test
     public void statusTest() {
 
         // 3.8.1.11 - Status
         // status
         //  : STATUS (';' other_param)* ':' statvalue CRLF
         //  ;
 
         String source = "STATUS:NEEDS-ACTION\n";
 
         ICalendarParser.StatusContext ctx = getParser(source).status();
 
         assertThat( ctx.k_status().getText(), is("STATUS") );
         assertThat( ctx.statvalue().getText(), is("NEEDS-ACTION") );
     }
 
     @Test
     public void summaryTest() {
 
         // 3.8.1.12 - Summary
         // summary
         //  : SUMMARY summparam* ':' text CRLF
         //  ;
 
         String source = "SUMMARY:Department Party\n";
 
         ICalendarParser.SummaryContext ctx = getParser(source).summary();
 
         assertThat( ctx.k_summary().getText(), is("SUMMARY") );
         assertThat( ctx.text().getText(), is("Department Party") );
     }
 
     @Test
     public void completedTest() {
 
         //  3.8.2.1 - Date-Time Completed
         // completed
         //  : k_completed (SCOL other_param)* COL date_time CRLF
         //  ;
 
         String source = "COMPLETED:19960401T150000Z\n";
         ICalendarParser.CompletedContext ctx = getParser(source).completed();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void dtendTest() {
 
         //  3.8.2.2 - Date-Time End
         // dtend
         //  : k_dtend dtendparam* COL date_time_date CRLF
         //  ;
 
         String source = "DTEND:19960401T150000Z\n";
         ICalendarParser.DtendContext ctx = getParser(source).dtend();
         assertThat( ctx.getText(), is(source) );
 
         source = "DTEND;VALUE=DATE:19980704\n";
         ctx = getParser(source).dtend();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void dueTest() {
 
         //  3.8.2.3 - Date-Time Due
         // due
         //  : k_due dueparam* COL date_time_date CRLF
         //  ;
 
         String source = "DUE:19980430T000000Z\n";
         ICalendarParser.DueContext ctx = getParser(source).due();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void dtstartTest() {
 
         //  3.8.2.4 - Date-Time Start
         // dtstart
         //  : k_dtstart dtstparam* COL date_time_date CRLF
         //  ;
 
         String source = "DTSTART:19980118T073000Z\n";
         ICalendarParser.DtstartContext ctx = getParser(source).dtstart();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void durationTest() {
 
         //  3.8.2.5 - Duration
         // duration
         //  : k_duration (SCOL other_param)* COL dur_value CRLF
         //  ;
 
         String source = "DURATION:PT1H0M0S\n";
         ICalendarParser.DurationContext ctx = getParser(source).duration();
         assertThat( ctx.getText(), is(source) );
 
         source = "DURATION:PT15M\n";
         ctx = getParser(source).duration();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void freebusyTest() {
 
         //  3.8.2.6 - Free/Busy Time
         // freebusy
         //  : k_freebusy fbparam* COL fbvalue CRLF
         //  ;
 
         String source = "FREEBUSY;FBTYPE=BUSY-UNAVAILABLE:19970308T160000Z/PT8H30M\n";
         ICalendarParser.FreebusyContext ctx = getParser(source).freebusy();
         assertThat( ctx.getText(), is(source) );
 
         source = "FREEBUSY;FBTYPE=FREE:19970308T160000Z/PT3H,19970308T200000Z/PT1H\n";
         ctx = getParser(source).freebusy();
         assertThat( ctx.getText(), is(source) );
 
         source = "FREEBUSY;FBTYPE=FREE:19970308T160000Z/PT3H,19970308T200000Z/PT1H\n" +
                 " ,19970308T230000Z/19970309T000000Z\n";
         ctx = getParser(source).freebusy();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
     }
 
     @Test
     public void transpTest() {
 
         //  3.8.2.7 - Time Transparency
         // transp
         //  : k_transp (SCOL other_param)* COL transvalue CRLF
         //  ;
 
         String source = "TRANSP:TRANSPARENT\n";
         ICalendarParser.TranspContext ctx = getParser(source).transp();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void tzidTest() {
 
         // 3.8.3.1 - Time Zone Identifier
         // tzid
         //  : k_tzid (SCOL other_param)* COL FSLASH? text CRLF
         //  ;
 
         String source = "TZID:America/New_York\n";
         ICalendarParser.TzidContext ctx = getParser(source).tzid();
         assertThat( ctx.getText(), is(source) );
 
         source = "TZID:/example.org/America/New_York\n";
         ctx = getParser(source).tzid();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void tznameTest() {
 
         // 3.8.3.2.  Time Zone Name
         // tzname
         //  : k_tzname tznparam* COL text CRLF
         //  ;
 
         String source = "TZNAME:EST\n";
         ICalendarParser.TznameContext ctx = getParser(source).tzname();
         assertThat( ctx.getText(), is(source) );
 
         source = "TZNAME;LANGUAGE=fr-CA:HNE\n";
         ctx = getParser(source).tzname();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void tzoffsetfromTest() {
 
         // 3.8.3.3 - Time Zone Offset From
         // tzoffsetfrom
         //  : k_tzoffsetfrom (SCOL other_param)* COL utc_offset CRLF
         //  ;
 
         String source = "TZOFFSETFROM:-0500\n";
         ICalendarParser.TzoffsetfromContext ctx = getParser(source).tzoffsetfrom();
         assertThat( ctx.getText(), is(source) );
 
         source = "TZOFFSETFROM:+1345\n";
         ctx = getParser(source).tzoffsetfrom();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void tzoffsettoTest() {
 
         // 3.8.3.4 - Time Zone Offset To
         // tzoffsetto
         //  : k_tzoffsetto (SCOL other_param)* COL utc_offset CRLF
         //  ;
 
         String source = "TZOFFSETTO:-0400\n";
         ICalendarParser.TzoffsettoContext ctx = getParser(source).tzoffsetto();
         assertThat( ctx.getText(), is(source) );
 
         source = "TZOFFSETTO:+1245\n";
         ctx = getParser(source).tzoffsetto();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void tzurlTest() {
 
         // 3.8.3.5.  Time Zone URL
         // tzurl
         //  : k_tzurl (SCOL other_param)* COL uri CRLF
         //  ;
 
         String source = "TZURL:http://timezones.example.org/tz/America-Los_Angeles.ics\n";
         ICalendarParser.TzurlContext ctx = getParser(source).tzurl();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void attendeeTest() {
 
         // 3.8.4.1 - Attendee
         // attendee
         //  : k_attendee attparam* COL cal_address CRLF
         //  ;
 
         String source = "ATTENDEE;MEMBER=\"mailto:DEV-GROUP@example.com\":\n" +
                 " mailto:joecool@example.com\n";
         ICalendarParser.AttendeeContext ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;DELEGATED-FROM=\"mailto:immud@example.com\":\n" +
                 " mailto:ildoit@example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=TENTATIVE;CN=Henry\n" +
                 " Cabot:mailto:hcabot@example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;ROLE=REQ-PARTICIPANT;DELEGATED-FROM=\"mailto:bob@\n" +
                 " example.com\";PARTSTAT=ACCEPTED;CN=Jane Doe:mailto:jdoe@\n" +
                 " example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;CN=John Smith;DIR=\"ldap://example.com:6666/o=ABC%\n" +
                 " 20Industries,c=US???(cn=Jim%20Dolittle)\":mailto:jimdo@\n" +
                 " example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=TENTATIVE;DELEGATED-FROM=\n" +
                 " \"mailto:iamboss@example.com\";CN=Henry Cabot:mailto:hcabot@\n" +
                 " example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;ROLE=NON-PARTICIPANT;PARTSTAT=DELEGATED;DELEGATED-TO=\n" +
                 " \"mailto:hcabot@example.com\";CN=The Big Cheese:mailto:iamboss\n" +
                 " @example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;CN=Jane Doe\n" +
                 " :mailto:jdoe@example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ATTENDEE;SENT-BY=mailto:jan_doe@example.com;CN=John Smith:\n" +
                 " mailto:jsmith@example.com\n";
         ctx = getParser(source).attendee();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
     }
 
     @Test
     public void contactTest() {
 
         // 3.8.4.2 - Contact
         // contact
         //  : k_contact contparam* COL text CRLF
         //  ;
 
         String source = "CONTACT:Jim Dolittle\\, ABC Industries\\, +1-919-555-1234\n";
         ICalendarParser.ContactContext ctx = getParser(source).contact();
         assertThat( ctx.getText(), is(source) );
 
         source = "CONTACT;ALTREP=\"ldap://example.com:6666/o=ABC%20Industries\\,\n" +
                 " c=US???(cn=Jim%20Dolittle)\":Jim Dolittle\\, ABC Industries\\,\n" +
                 " +1-919-555-1234\n";
         ctx = getParser(source).contact();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "CONTACT;ALTREP=\"CID:part3.msg970930T083000SILVER@example.com\":\n" +
                 " Jim Dolittle\\, ABC Industries\\, +1-919-555-1234\n";
         ctx = getParser(source).contact();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "CONTACT;ALTREP=\"http://example.com/pdi/jdoe.vcf\":Jim\n" +
                 " Dolittle\\, ABC Industries\\, +1-919-555-1234\n";
         ctx = getParser(source).contact();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
     }
 
     @Test
     public void organizerTest() {
 
         // 3.8.4.3 - Organizer
         // organizer
         //  : k_organizer orgparam* COL cal_address CRLF
         //  ;
 
         String source = "ORGANIZER;CN=John Smith:mailto:jsmith@example.com\n";
         ICalendarParser.OrganizerContext ctx = getParser(source).organizer();
         assertThat( ctx.getText(), is(source) );
 
         source = "ORGANIZER;CN=JohnSmith;DIR=\"ldap://example.com:6666/o=DC%20Ass\n" +
                 " ociates,c=US???(cn=John%20Smith)\":mailto:jsmith@example.com\n";
         ctx = getParser(source).organizer();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "ORGANIZER;SENT-BY=\"mailto:jane_doe@example.com\":\n" +
                 " mailto:jsmith@example.com\n";
         ctx = getParser(source).organizer();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
     }
 
     @Test
     public void recuridTest() {
 
         // 3.8.4.4 - Recurrence ID
         // recurid
         //  : k_recurrence_id ridparam* COL date_time_date CRLF
         //  ;
 
         String source = "RECURRENCE-ID;VALUE=DATE:19960401\n";
         ICalendarParser.RecuridContext ctx = getParser(source).recurid();
         assertThat( ctx.getText(), is(source) );
 
         source = "RECURRENCE-ID;RANGE=THISANDFUTURE:19960120T120000Z\n";
         ctx = getParser(source).recurid();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void relatedTest() {
 
         // 3.8.4.5.  Related To
         // related
         //  : k_related_to relparam* COL text CRLF
         //  ;
 
         String source = "RELATED-TO:jsmith.part7.19960817T083000.xyzMail@example.com\n";
         ICalendarParser.RelatedContext ctx = getParser(source).related();
         assertThat( ctx.getText(), is(source) );
 
         source = "RELATED-TO:19960401-080045-4000F192713-0052@example.com\n";
         ctx = getParser(source).related();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void urlTest() {
 
         // 3.8.4.6 - Uniform Resource Locator
         // url
         //  : k_url (SCOL other_param)* COL uri CRLF
         //  ;
 
         String source = "URL:http://example.com/pub/calendars/jsmith/mytime.ics\n";
         ICalendarParser.UrlContext ctx = getParser(source).url();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void uidTest() {
 
         // 3.8.4.7 - Unique Identifier
         // uid
         //  : k_uid (SCOL other_param)* COL text CRLF
         //  ;
 
         String source = "UID:19960401T080045Z-4000F192713-0052@example.com\n";
         ICalendarParser.UidContext ctx = getParser(source).uid();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void exdateTest() {
 
         // 3.8.5.1 - Exception Date-Times
         // exdate
         //  : k_exdate exdtparam* COL date_time_date (COMMA date_time_date)* CRLF
         //  ;
 
         String source = "EXDATE:19960402T010000Z,19960403T010000Z,19960404T010000Z\n";
         ICalendarParser.ExdateContext ctx = getParser(source).exdate();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void rdateTest() {
 
         // 3.8.5.2 - Recurrence Date-Times
         // rdate
         //  : k_rdate rdtparam* COL rdtval (COMMA rdtval)* CRLF
         //  ;
 
         String source = "RDATE:19970714T123000Z\n";
         ICalendarParser.RdateContext ctx = getParser(source).rdate();
         assertThat( ctx.getText(), is(source) );
 
         source = "RDATE;TZID=America/New_York:19970714T083000\n";
         ctx = getParser(source).rdate();
         assertThat( ctx.getText(), is(source) );
 
         source = "RDATE;VALUE=PERIOD:19960403T020000Z/19960403T040000Z,\n" +
                 " 19960404T010000Z/PT3H\n";
         ctx = getParser(source).rdate();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "RDATE;VALUE=DATE:19970101,19970120,19970217,19970421,\n" +
                 " 19970526,19970704,19970901,19971014,19971128,19971129,19971225\n";
         ctx = getParser(source).rdate();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
     }
 
     @Test
     public void rruleTest() {
 
         // 3.8.5.3 - Recurrence Rule
         // rrule
         //  : k_rrule (SCOL other_param)* COL recur CRLF
         //  ;
 
         String source = "RRULE:FREQ=DAILY;COUNT=10\n";
         ICalendarParser.RruleContext ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=DAILY;UNTIL=19971224T000000Z\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=DAILY;INTERVAL=2\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;\n" +
                 " BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "RRULE:FREQ=DAILY;UNTIL=20000131T140000Z;BYMONTH=1\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;COUNT=10\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;UNTIL=19971224T000000Z\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;INTERVAL=2;WKST=SU\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;UNTIL=19971007T000000Z;WKST=SU;BYDAY=TU,TH\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;COUNT=10;WKST=SU;BYDAY=TU,TH\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;INTERVAL=2;UNTIL=19971224T000000Z;WKST=SU;\n" +
                 " BYDAY=MO,WE,FR\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=8;WKST=SU;BYDAY=TU,TH\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;COUNT=10;BYDAY=1FR\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;UNTIL=19971224T000000Z;BYDAY=1FR\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;INTERVAL=2;COUNT=10;BYDAY=1SU,-1SU\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;COUNT=6;BYDAY=-2MO\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;BYMONTHDAY=-3\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=2,15\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=1,-1\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;INTERVAL=18;COUNT=10;BYMONTHDAY=10,11,12,13,14,15\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=TU\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;COUNT=10;BYMONTH=6,7\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;INTERVAL=2;COUNT=10;BYMONTH=1,2,3\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;INTERVAL=3;COUNT=10;BYYEARDAY=1,100,200\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;BYDAY=20MO\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;BYWEEKNO=20;BYDAY=MO\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=TH\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;BYDAY=TH;BYMONTH=6,7,8\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;BYDAY=SA;BYMONTHDAY=7,8,9,10,11,12,13\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=YEARLY;INTERVAL=4;BYMONTH=11;BYDAY=TU;BYMONTHDAY=2,3,4,5,6,7,8\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
         assertThat( ctx.recur().recur_rule_part().size(), is(5) );
         assertThat( ctx.recur().recur_rule_part(0).freq().getText(), is("YEARLY") );
         assertThat( ctx.recur().recur_rule_part(1).digits().getText(), is("4") );
         assertThat( ctx.recur().recur_rule_part(2).bymolist().getText(), is("11") );
         assertThat( ctx.recur().recur_rule_part(3).bywdaylist().getText(), is("TU") );
         assertThat( ctx.recur().recur_rule_part(4).bymodaylist().getText(), is("2,3,4,5,6,7,8") );
 
         source = "RRULE:FREQ=MONTHLY;COUNT=3;BYDAY=TU,WE,TH;BYSETPOS=3\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=-2\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=HOURLY;INTERVAL=3;UNTIL=19970902T170000Z\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MINUTELY;INTERVAL=15;COUNT=6\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MINUTELY;INTERVAL=90;COUNT=4\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=DAILY;BYHOUR=9,10,11,12,13,14,15,16;BYMINUTE=0,20,40\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MINUTELY;INTERVAL=20;BYHOUR=9,10,11,12,13,14,15,16\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=4;BYDAY=TU,SU;WKST=MO\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=4;BYDAY=TU,SU;WKST=SU\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
 
         source = "RRULE:FREQ=MONTHLY;BYMONTHDAY=15,30;COUNT=5\n";
         ctx = getParser(source).rrule();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void actionTest() {
 
         // 3.8.6.1 - Action
         // action
         //  : k_action (SCOL other_param)* COL actionvalue CRLF
         //  ;
 
         String source = "ACTION:AUDIO\n";
         ICalendarParser.ActionContext ctx = getParser(source).action();
         assertThat( ctx.getText(), is(source) );
 
         source = "ACTION:DISPLAY\n";
         ctx = getParser(source).action();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void repeatTest() {
 
         // 3.8.6.2 - Repeat Count
         // repeat
         //  : k_repeat (SCOL other_param)* COL integer CRLF
         //  ;
 
         String source = "REPEAT:4\n";
         ICalendarParser.RepeatContext ctx = getParser(source).repeat();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void triggerTest() {
 
         // 3.8.6.3 - Trigger
         // trigger
         //  : k_trigger trigrel* COL dur_value CRLF
         //  | k_trigger trigabs* COL date_time CRLF
         //  ;
 
         String source = "TRIGGER:-PT15M\n";
         ICalendarParser.TriggerContext ctx = getParser(source).trigger();
         assertThat( ctx.getText(), is(source) );
 
         source = "TRIGGER;RELATED=END:PT5M\n";
         ctx = getParser(source).trigger();
         assertThat( ctx.getText(), is(source) );
 
         source = "TRIGGER;VALUE=DATE-TIME:19980101T050000Z\n";
         ctx = getParser(source).trigger();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void createdTest() {
 
         // 3.8.7.1 - Date-Time Created
         // created
         //  : k_created (SCOL other_param)* COL date_time CRLF
         //  ;
 
         String source = "CREATED:19960329T133000Z\n";
         ICalendarParser.CreatedContext ctx = getParser(source).created();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void dtstampTest() {
 
         // 3.8.7.2 - Date-Time Stamp
         // dtstamp
         //  : k_dtstamp (SCOL other_param)* COL date_time CRLF
         //  ;
 
         String source = "\n";
         ICalendarParser.DtstampContext ctx = getParser(source).dtstamp();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void last_modTest() {
 
         // 3.8.7.3 - Last Modified
         // last_mod
         //  : k_last_modified (SCOL other_param)* COL date_time CRLF
         //  ;
 
         String source = "LAST-MODIFIED:19960817T133000Z\n";
 
         ICalendarParser.Last_modContext ctx = getParser(source).last_mod();
 
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void seqTest() {
 
         // 3.8.7.4 - Sequence Number
         // seq
         //  : k_sequence (SCOL other_param)* COL integer CRLF
         //  ;
 
         String source = "SEQUENCE:0\n";
         ICalendarParser.SeqContext ctx = getParser(source).seq();
         assertThat( ctx.getText(), is(source) );
 
         source = "SEQUENCE:+999\n";
         ctx = getParser(source).seq();
         assertThat( ctx.getText(), is(source) );
 
         source = "SEQUENCE:-5\n";
         ctx = getParser(source).seq();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void iana_propTest() {
 
         // 3.8.8.1 - IANA Properties
         // iana_prop
         //  : iana_token (SCOL icalparameter)* COL value CRLF
         //  ;
 
         String source = "DRESSCODE:CASUAL\n";
         ICalendarParser.Iana_propContext ctx = getParser(source).iana_prop();
         assertThat( ctx.getText(), is(source) );
 
         source = "NON-SMOKING;VALUE=BOOLEAN:TRUE\n";
         ctx = getParser(source).iana_prop();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void x_propTest() {
 
         // 3.8.8.2 - Non-Standard Propertie
         // x_prop
         //  : x_name (SCOL icalparameter)* COL value CRLF
         //  ;
 
         String source = "X-ABC-MMSUBJ;VALUE=URI;FMTTYPE=audio/basic:http://www.example.org/mysubj.au\n";
         ICalendarParser.X_propContext ctx = getParser(source).x_prop();
         assertThat( ctx.getText(), is(source) );
     }
 
     @Test
     public void rstatusTest() {
 
         // 3.8.8.3 - Request Status
         // rstatus
         //  : k_request_status rstatparam* COL statcode SCOL text (SCOL text)?
         //  ;
 
         String source = "REQUEST-STATUS:2.0;Success";
         ICalendarParser.RstatusContext ctx = getParser(source).rstatus();
         assertThat( ctx.getText(), is(source) );
 
         source = "REQUEST-STATUS:3.1;Invalid property value;DTSTART:96-Apr-01";
         ctx = getParser(source).rstatus();
         assertThat( ctx.getText(), is(source) );
 
         source = "REQUEST-STATUS:2.8; Success\\, repeating event ignored. Scheduled\n" +
                 " as a single event.;RRULE:FREQ=WEEKLY\\;INTERVAL=2";
         ctx = getParser(source).rstatus();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
 
         source = "REQUEST-STATUS:4.1;Event conflict.  Date-time is busy.";
         ctx = getParser(source).rstatus();
         assertThat( ctx.getText(), is(source) );
 
         source = "REQUEST-STATUS:3.7;Invalid calendar user;ATTENDEE:\n" +
                 " mailto:jsmith@example.com";
         ctx = getParser(source).rstatus();
         assertThat( ctx.getText(), is(source.replace("\n ", "")) );
     }
 }
 
