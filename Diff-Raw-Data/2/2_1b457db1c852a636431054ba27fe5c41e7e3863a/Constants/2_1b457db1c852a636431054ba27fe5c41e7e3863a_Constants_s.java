 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.td_import;
 
 public interface Constants extends com.treasure_data.client.Constants {
     String CMD_TABLEIMPORT = "table_import";
 
     String CMD_TABLEIMPORT_USAGE = "  $ td table:import <db> <table> <files...>\n";
 
     String CMD_TABLEIMPORT_EXAMPLE =
             "  $ td table:import example_db table1 --apache access.log\n" +
             "  $ td table:import example_db table1 --json -t time - < test.json\n";
 
     String CMD_TABLEIMPORT_DESC = "  Parse and import files to a table\n";
 
     String CMD_TABLEIMPORT_OPTIONS =
             "      --format FORMAT              file format (default: apache)\n" +
             "      --apache                     same as --format apache; apache common log format\n" +
             "      --syslog                     same as --format syslog; syslog\n" +
             "      --msgpack                    same as --format msgpack; msgpack stream format\n" +
             "      --json                       same as --format json; LF-separated json format\n" +
             "  -t, --time-key COL_NAME          time key name for json and msgpack format (e.g. 'created_at')\n" +
             "      --auto-create-table          Create table and database if doesn't exist\n";
 
     String CMD_PREPARE = "prepare";
 
     String CMD_PREPARE_USAGE = "  $ td import:prepare <files...>\n";
 
     String CMD_PREPARE_EXAMPLE =
             "  $ td import:prepare logs/*.csv --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
             "  $ td import:prepare mytable --format mysql --db-url jdbc:mysql://localhost/mydb --db-user myuser --db-password mypass\n";
 
     String CMD_PREPARE_DESC = "  Convert files into part file format\n";
 
     String CMD_PREPARE_OPTIONS =
             "    -f, --format FORMAT              source file format [csv, tsv, json, msgpack, mysql]; default=csv\n" +
             "    -C, --compress TYPE              compressed type [gzip, none, auto]; default=auto detect\n" +
             "    -T, --time-format FORMAT         STRF_FORMAT; strftime(3) format of the time column\n" +
             "    -e, --encoding TYPE              encoding type [utf-8]\n" +
             "    -o, --output DIR                 output directory\n" +
             "    -s, --split-size SIZE_IN_KB      size of each parts (default: 16384)\n" +
             "    -t, --time-column NAME           name of the time column\n" +
             "    --time-value TIME                long value of the time column\n" +
             "    --prepare-parallel NUM           prepare in parallel (default: 2; max 8)\n" +
             "    --only-columns NAME,NAME,...     only columns\n" +
             "    --exclude-columns NAME,NAME,...  exclude columns\n" +
             "    --error-records-handling MODE    error records handling mode [skip, abort]; default=skip\n" +
             "    --columns NAME,NAME,...          column names (use --column-header instead if the first line has column names)\n" +
             "    --column-types TYPE,TYPE,...     column types [string, int, long]\n" +
             "\n" +
             "    CSV/TSV specific options:\n" +
             "    --column-header                  first line includes column names\n" +
             "    --delimiter CHAR                 delimiter CHAR; default=\",\" at csv, \"\\t\" at tsv\n" +
            "    --newline TYPE                   newline [CRLR, LR, CR];  default=CRLF\n" +
             "    --quote CHAR                     quote [DOUBLE, SINGLE]; default=DOUBLE\n" +
             "\n" +
             "    MySQL specific options:\n" +
             "    --db-url URL                     JDBC connection URL\n" +
             "    --db-user NAME                   user name for MySQL account\n" +
             "    --db-password PASSWORD           password for MySQL account\n";
 
     String CMD_UPLOAD = "upload";
 
     String CMD_UPLOAD_USAGE =
             "  $ td import:upload <session name> <files...>\n";
 
     String CMD_UPLOAD_EXAMPLE =
             "  $ td import:upload mysess parts/* --parallel 4\n" +
             "  $ td import:upload mysess parts/*.csv --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
             "  $ td import:upload parts/*.csv --auto-create mydb.mytbl --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
             "  $ td import:upload mysess mytable --format mysql --db-url jdbc:mysql://localhost/mydb --db-user myuser --db-password mypass\n";
 
     String CMD_UPLOAD_DESC = "  Upload or re-upload files into a bulk import session";
 
     String CMD_UPLOAD_OPTIONS =
             "    --auto-create DATABASE.TABLE     create automatically bulk import session by specified database and table names\n" +
             "                                     If you use 'auto-create' option, you MUST not specify any session name as first argument.\n" +
             "    --auto-perform                   perform bulk import job automatically\n" +
             "    --auto-commit                    commit bulk import job automatically\n" +
             "    --auto-delete                    delete bulk import session automatically\n" +
             "    --parallel NUM                   upload in parallel (default: 2; max 8)\n" +
             "\n" +
             CMD_PREPARE_OPTIONS;
 
     String CMD_AUTO = "auto";
     String CMD_AUTO_ENABLE = "td.bulk_import.auto.enable";
 
     String CMD_AUTO_USAGE =
             "  $ td import:auto <session name> <files...>\n";
 
     String CMD_AUTO_EXAMPLE =
             "  $ td import:auto mysess parts/* --parallel 4\n" +
             "  $ td import:auto mysess parts/*.csv --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
             "  $ td import:auto parts/*.csv --auto-create mydb.mytbl --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
             "  $ td import:auto mysess mytable --format mysql --db-url jdbc:mysql://localhost/mydb --db-user myuser --db-password mypass\n";
 
     String CMD_AUTO_DESC = "  Automatically upload or re-upload files into a bulk import session. "
             + "It's functional equivalent of 'upload' command with 'auto-perform', 'auto-commit' and 'auto-delete' options. "
             + "But it, by default, doesn't provide 'auto-create' option. "
             + "If you want 'auto-create' option, you explicitly must declare it as command options.\n";
 
     String CMD_AUTO_OPTIONS =
             "    --auto-create DATABASE.TABLE     create automatically bulk import session by specified database and table names\n" +
             "                                     If you use 'auto-create' option, you MUST not specify any session name as first argument.\n" +
             "    --parallel NUM                   upload in parallel (default: 2; max 8)\n" +
             "\n" +
             CMD_PREPARE_OPTIONS;
 
     String STAT_SUCCESS = "SUCCESS";
     String STAT_ERROR = "ERROR";
 
     ////////////////////////////////////////
     // OPTIONS                            //
     ////////////////////////////////////////
 
     // help
     String BI_PREPARE_PARTS_HELP = "help";
     String HYPHENHYPHEN = "--";
     String BI_PREPARE_PARTS_HELP_DESC = "show this help message";
 
     ////////////////////////////////////////
     // TABLE_IMPORT_OPTIONS               //
     ////////////////////////////////////////
 
     // format
     String TABLE_IMPORT_FORMAT_DESC = "file format (default: apache)"; // default 'apache'
     String TABLE_IMPORT_FORMAT_APACHE = "apache";
     String TABLE_IMPORT_FORMAT_APACHE_DESC = "same as --format apache; apache common log format";
     String TABLE_IMPORT_FORMAT_SYSLOG = "syslog";
     String TABLE_IMPORT_FORMAT_SYSLOG_DESC = "same as --format syslog; syslog";
     String TABLE_IMPORT_FORMAT_MSGPACK = "msgpack";
     String TABLE_IMPORT_FORMAT_MSGPACK_DESC = "same as --format msgpack; msgpack stream format";
     String TABLE_IMPORT_FORMAT_JSON = "json";
     String TABLE_IMPORT_FORMAT_JSON_DESC = "same as --format json; LF-separated json format";
     String TABLE_IMPORT_FORMAT_DEFAULTVALUE = TABLE_IMPORT_FORMAT_APACHE;
 
     // time-key
     String TABLE_IMPORT_TIME_KEY = "time-key";
     String TABLE_IMPORT_TIME_KEY_DESC = "time key name for json and msgpack format (e.g. 'created_at')";
 
     // auto-create-table
     String TABLE_IMPORT_AUTO_CREATE_TABLE = "auto-create-table";
     String TABLE_IMPORT_AUTO_CREATE_TABLE_DESC = "Create table and database if doesn't exist";
 
     ////////////////////////////////////////
     // UPLOAD_PARTS_OPTIONS               //
     ////////////////////////////////////////
 
     // format
     String BI_UPLOAD_PARTS_FORMAT_DEFAULTVALUE = "msgpack.gz"; // default 'msgpack.gz'
 
     // auto-craete
     String BI_UPLOAD_PARTS_AUTO_CREATE = "auto-create";
     String BI_UPLOAD_AUTO_CREATE_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_CREATE;
     String BI_UPLOAD_PARTS_AUTO_CREATE_DESC =
             "create automatically bulk import session by specified database and table names";
 
     // auto-delete
     String BI_UPLOAD_PARTS_AUTO_DELETE = "auto-delete";
     String BI_UPLOAD_AUTO_DELETE_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_DELETE;
     String BI_UPLOAD_PARTS_AUTO_DELETE_DESC = "delete bulk import session automatically";
 
     // auto-perform
     String BI_UPLOAD_PARTS_AUTO_PERFORM = "auto-perform";
     String BI_UPLOAD_AUTO_PERFORM_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_PERFORM;
     String BI_UPLOAD_PARTS_AUTO_PERFORM_DEFAULTVALUE = "false";
     String BI_UPLOAD_PARTS_AUTO_PERFORM_DESC = "perform bulk import job automatically";
 
     // auto-commit
     String BI_UPLOAD_PARTS_AUTO_COMMIT = "auto-commit";
     String BI_UPLOAD_AUTO_COMMIT_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_COMMIT;
     String BI_UPLOAD_PARTS_AUTO_COMMIT_DEFAULTVALUE = "false";
     String BI_UPLOAD_PARTS_AUTO_COMMIT_DESC = "commit bulk import job automatically";
 
     // parallel NUM
     String BI_UPLOAD_PARTS_PARALLEL = "parallel";
     String BI_UPLOAD_AUTO_PARALLEL_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_PARALLEL;
     String BI_UPLOAD_PARTS_PARALLEL_DEFAULTVALUE = "2";
     String BI_UPLOAD_PARTS_PARALLEL_MAX_VALUE = "8";
     String BI_UPLOAD_PARTS_PARALLEL_DESC = "upload in parallel (default: 2; max 8)";
 
     // retryCount NUM
     String BI_UPLOAD_PARTS_RETRYCOUNT = "td.bulk_import.upload_parts.retrycount";
     String BI_UPLOAD_PARTS_RETRYCOUNT_DEFAULTVALUE = "10";
 
     // waitSec NUM
     String BI_UPLOAD_PARTS_WAITSEC = "td.bulk_import.upload_parts.waitsec";
     String BI_UPLOAD_PARTS_WAITSEC_DEFAULTVALUE = "1";
 
     ////////////////////////////////////////
     // PREPARE_PARTS_OPTIONS              //
     ////////////////////////////////////////
 
     // format [csv, tsv, json, msgpack, apache, regexp]; default=auto detect
     String BI_PREPARE_PARTS_FORMAT = "format";
     String BI_PREPARE_FORMAT_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_FORMAT;
     String BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE = "csv"; // default 'csv'
     String BI_PREPARE_PARTS_FORMAT_DESC = "source file format [csv, tsv, json, msgpack]; default=csv";
 
     // output format [msgpackgz]; default=msgpackgz
     String BI_PREPARE_PARTS_OUTPUTFORMAT = "td.bulk_import.prepare_parts.outputformat";
     String BI_PREPARE_PARTS_OUTPUTFORMAT_DEFAULTVALUE = "msgpackgz";
 
     // compress [gzip,.., auto]; default=auto detect
     String BI_PREPARE_PARTS_COMPRESSION = "compress";
     String BI_PREPARE_COMPRESSION_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COMPRESSION;
     String BI_PREPARE_PARTS_COMPRESSION_DEFAULTVALUE = "auto";
     String BI_PREPARE_PARTS_COMPRESSION_DESC = "compressed type [gzip, none]; default=auto detect";
 
     // parallel
     String BI_PREPARE_PARTS_PARALLEL = "prepare-parallel";
     String BI_PREPARE_PARALLEL_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_PARALLEL;
     String BI_PREPARE_PARTS_PARALLEL_DEFAULTVALUE = "1";
     String BI_PREPARE_PARTS_PARALLEL_DESC = "prepare in parallel (default: 2; max 8)";
 
     // encoding [utf-8,...]
     String BI_PREPARE_PARTS_ENCODING = "encoding";
     String BI_PREPARE_ENCODING_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ENCODING;
     String BI_PREPARE_PARTS_ENCODING_DEFAULTVALUE = "UTF-8";
     String BI_PREPARE_PARTS_ENCODING_DESC = "encoding type [UTF-8]; default=UTF-8";
 
     // columns, column-types
     String BI_PREPARE_PARTS_COLUMNS = "columns";
     String BI_PREPARE_COLUMNS_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COLUMNS;
     String BI_PREPARE_PARTS_COLUMNS_DESC = "column names (use --column-header instead if the first line has column names)";
     String BI_PREPARE_PARTS_COLUMNTYPES = "column-types";
     String BI_PREPARE_COLUMNTYPES_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COLUMNTYPES;
     String BI_PREPARE_PARTS_COLUMNTYPES_DESC = "column types [string, int, long]";
 
     // exclude-columns, only-columns
     String BI_PREPARE_PARTS_EXCLUDE_COLUMNS = "exclude-columns";
     String BI_PREPARE_EXCLUDE_COLUMNS_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_EXCLUDE_COLUMNS;
     String BI_PREPARE_PARTS_EXCLUDE_COLUMNS_DESC = "exclude columns";
     String BI_PREPARE_PARTS_ONLY_COLUMNS = "only-columns";
     String BI_PREPARE_ONLY_COLUMNS_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ONLY_COLUMNS;
     String BI_PREPARE_PARTS_ONLY_COLUMNS_DESC = "only columns";
 
     // time-column NAME; default='time'
     String BI_PREPARE_PARTS_TIMECOLUMN = "time-column";
     String BI_PREPARE_TIMECOLUMN_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_TIMECOLUMN;
     String BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE = "time";
     String BI_PREPARE_PARTS_TIMECOLUMN_DESC = "name of the time column";
 
     // time-value
     String BI_PREPARE_PARTS_TIMEVALUE = "time-value";
     String BI_PREPARE_TIMEVALUE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_TIMEVALUE;
     String BI_PREPARE_PARTS_TIMEVALUE_DESC = "long value of the time column";
 
     // time-format STRF_FORMAT; default=auto detect
     String BI_PREPARE_PARTS_TIMEFORMAT = "time-format";
     String BI_PREPARE_TIMEFORMAT_HYPHEN_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_TIMEFORMAT;
     String BI_PREPARE_PARTS_TIMEFORMAT_DESC = "STRF_FORMAT; strftime(3) format of the time column";
 
     // output DIR
     String BI_PREPARE_PARTS_OUTPUTDIR = "output";
     String BI_PREPARE_OUTPUTDIR_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_OUTPUTDIR;
     String BI_PREPARE_PARTS_OUTPUTDIR_DEFAULTVALUE = "out"; // './out/'
     String BI_PREPARE_PARTS_OUTPUTDIR_DESC = "output directory";
 
     // error handling
     String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING = "error-records-handling";
     String BI_PREPARE_ERROR_RECORDS_HANDLING_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING;
     String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DEFAULTVALUE= "skip";
     String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DESC = "error records handling mode [skip, abort]; default=skip";
 
     // dry-run; show samples as JSON and exit
     String BI_PREPARE_PARTS_DRYRUN = "td.bulk_import.prepare_parts.dry-run";
     String BI_PREPARE_PARTS_DRYRUN_DEFAULTVALUE = "false";
 
     String BI_PREPARE_PARTS_SPLIT_SIZE = "split-size";
     String BI_PREPARE_SPLIT_SIZE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_SPLIT_SIZE;
     String BI_PREPARE_PARTS_SPLIT_SIZE_DEFAULTVALUE ="16384";
     String BI_PREPARE_PARTS_SPLIT_SIZE_DESC = "size of each parts (default: 16384)";
 
     ////////////////////////////////////////
     // CSV/TSV PREPARE_PARTS_OPTIONS      //
     ////////////////////////////////////////
 
     // quote [DOUBLE, SINGLE]; default=DOUBLE
     String BI_PREPARE_PARTS_QUOTE = "quote";
     String BI_PREPARE_QUOTE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_QUOTE;
     String BI_PREPARE_PARTS_QUOTE_DEFAULTVALUE = "DOUBLE";
     String BI_PREPARE_PARTS_QUOTE_DESC = "quote [DOUBLE, SINGLE]; default=DOUBLE";
 
     // delimiter CHAR; default=',' at 'csv', '\t' at 'tsv'
     String BI_PREPARE_PARTS_DELIMITER = "delimiter";
     String BI_PREPARE_DELIMITER_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_DELIMITER;
     String BI_PREPARE_PARTS_DELIMITER_CSV_DEFAULTVALUE = ",";
     String BI_PREPARE_PARTS_DELIMITER_TSV_DEFAULTVALUE = "\t";
     String BI_PREPARE_PARTS_DELIMITER_DESC = "delimiter CHAR; default=\",\" at csv, \"\\t\" at tsv";
 
     // newline [CRLF, LF, CR]; default=CRLF (or auto detect?)
     String BI_PREPARE_PARTS_NEWLINE = "newline";
     String BI_PREPARE_NEWLINE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_NEWLINE;
     String BI_PREPARE_PARTS_NEWLINE_DEFAULTVALUE = "CRLF"; // default CRLF
     String BI_PREPARE_PARTS_NEWLINE_DESC = "newline [CRLR, LR, CR];  default=CRLF";
 
     // column-header; default=true
     String BI_PREPARE_PARTS_COLUMNHEADER = "column-header";
     String BI_PREPARE_COLUMNHEADER_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COLUMNHEADER;
     String BI_PREPARE_PARTS_COLUMNHEADER_DEFAULTVALUE = "false";
     String BI_PREPARE_PARTS_COLUMNHEADER_DESC = "first line includes column names";
 
     // type-conversion-error [skip,null]; default=skip
     String BI_PREPARE_PARTS_TYPE_CONVERSION_ERROR_DEFAULTVALUE = "skip";
 
     String BI_PREPARE_PARTS_SAMPLE_ROWSIZE = "td.bulk_import.prepare_parts.sample.rowsize";
     String BI_PREPARE_PARTS_SAMPLE_ROWSIZE_DEFAULTVALUE = "30";
 
     ////////////////////////////////////////
     // MYSQL PREPARE_PARTS_OPTIONS        //
     ////////////////////////////////////////
 
     String BI_PREPARE_PARTS_MYSQL_JDBCDRIVER_CLASS = "com.mysql.jdbc.Driver";
 
     // url
     String BI_PREPARE_PARTS_JDBC_CONNECTION_URL = "db-url";
     String BI_PREPARE_PARTS_JDBC_CONNECTION_URL_DESC = "JDBC connection URL";
 
     // user
     String BI_PREPARE_PARTS_JDBC_USER = "db-user";
     String BI_PREPARE_PARTS_JDBC_USER_DESC = "user name for MySQL account";
 
     // password
     String BI_PREPARE_PARTS_JDBC_PASSWORD = "db-password";
     String BI_PREPARE_PARTS_JDBC_PASSWORD_DESC = "password for MySQL account";
 }
