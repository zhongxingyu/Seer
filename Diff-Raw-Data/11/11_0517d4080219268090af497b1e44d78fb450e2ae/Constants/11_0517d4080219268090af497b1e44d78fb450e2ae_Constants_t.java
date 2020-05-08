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
 package com.treasure_data.bulk_import;
 
 public interface Constants extends com.treasure_data.client.Constants {
 
     String CMD_PREPARE = "prepare";
     String CMD_PREPARE_USAGE =
            "  $ td import:prepare <files...>";
     String CMD_PREPARE_EXAMPLE =
            "  $ td import:prepare logs/*.csv --format csv --columns time,uid,price,count --time-column time -o parts/";
     String CMD_PREPARE_DESC =
             "  Convert files into part file format";
     String CMD_UPLOAD = "upload";
     String CMD_UPLOAD_USAGE =
            "  $ td import:upload <name> <files...>";
     String CMD_UPLOAD_EXAMPLE =
            "  $ td import:upload parts/* --parallel 4";
     String CMD_UPLOAD_DESC =
             "  Upload or re-upload files into a bulk import session";
 
     String STAT_SUCCESS = "SUCCESS";
     String STAT_ERROR = "ERROR";
 
     ////////////////////////////////////////
     // UPLOAD_PARTS_OPTIONS               //
     ////////////////////////////////////////
 
     // format
     String BI_UPLOAD_PARTS_FORMAT_DEFAULTVALUE = "msgpack.gz"; // default 'msgpack.gz'
 
     // auto-craete
     String BI_UPLOAD_PARTS_AUTO_CREATE = "auto-create";
     String BI_UPLOAD_PARTS_AUTO_CREATE_DESC =
             "create automatically bulk import session by specified database and table names";
 
     // auto-delete
     String BI_UPLOAD_PARTS_AUTO_DELETE = "auto-delete";
     String BI_UPLOAD_PARTS_AUTO_DELETE_DESC = "delete bulk import session automatically";
 
     // auto-perform
     String BI_UPLOAD_PARTS_AUTO_PERFORM = "auto-perform";
     String BI_UPLOAD_PARTS_AUTO_PERFORM_DEFAULTVALUE = "false";
     String BI_UPLOAD_PARTS_AUTO_PERFORM_DESC = "perform bulk import job automatically";
 
     // auto-commit
     String BI_UPLOAD_PARTS_AUTO_COMMIT = "auto-commit";
     String BI_UPLOAD_PARTS_AUTO_COMMIT_DEFAULTVALUE = "false";
     String BI_UPLOAD_PARTS_AUTO_COMMIT_DESC = "commit bulk import job automatically";
 
     // parallel NUM
     String BI_UPLOAD_PARTS_PARALLEL = "parallel";
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
 
     // help
     String BI_PREPARE_PARTS_HELP = "help";
     String BI_PREPARE_PARTS_HELP_DESC = "show this help message";
 
     // format [csv, tsv, json, msgpack, apache, regexp]; default=auto detect
     String BI_PREPARE_PARTS_FORMAT = "format";
     String BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE = "csv"; // default 'csv'
     String BI_PREPARE_PARTS_FORMAT_DESC = "source file format [csv, tsv, json, msgpack]; default=csv";
 
     // output format [msgpackgz]; default=msgpackgz
     String BI_PREPARE_PARTS_OUTPUTFORMAT = "td.bulk_import.prepare_parts.outputformat";
     String BI_PREPARE_PARTS_OUTPUTFORMAT_DEFAULTVALUE = "msgpackgz";
 
     // compress [gzip,.., auto]; default=auto detect
     String BI_PREPARE_PARTS_COMPRESSION = "compress";
     String BI_PREPARE_PARTS_COMPRESSION_DEFAULTVALUE = "auto";
     String BI_PREPARE_PARTS_COMPRESSION_DESC = "compressed type [gzip, none]; default=auto detect";
 
     // parallel
     String BI_PREPARE_PARTS_PARALLEL = "prepare-parallel";
     String BI_PREPARE_PARTS_PARALLEL_DEFAULTVALUE = "1";
     String BI_PREPARE_PARTS_PARALLEL_DESC = "prepare in parallel (default: 2; max 8)";
 
     // encoding [utf-8,...]
     String BI_PREPARE_PARTS_ENCODING = "encoding";
     String BI_PREPARE_PARTS_ENCODING_DEFAULTVALUE = "UTF-8";
     String BI_PREPARE_PARTS_ENCODING_DESC = "encoding type [utf-8]";
 
     // columns, column-types
     String BI_PREPARE_PARTS_COLUMNS = "columns";
     String BI_PREPARE_PARTS_COLUMNS_DESC = "column names (use --column-header instead if the first line has column names)";
     String BI_PREPARE_PARTS_COLUMNTYPES = "column-types";
     String BI_PREPARE_PARTS_COLUMNTYPES_DESC = "column types [string, int, long]";
 
     // exclude-columns, only-columns
     String BI_PREPARE_PARTS_EXCLUDE_COLUMNS = "exclude-columns";
     String BI_PREPARE_PARTS_EXCLUDE_COLUMNS_DESC = "exclude columns";
     String BI_PREPARE_PARTS_ONLY_COLUMNS = "only-columns";
     String BI_PREPARE_PARTS_ONLY_COLUMNS_DESC = "only columns";
 
     // time-column NAME; default='time'
     String BI_PREPARE_PARTS_TIMECOLUMN = "time-column";
     String BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE = "time";
     String BI_PREPARE_PARTS_TIMECOLUMN_DESC = "name of the time column";
 
     // time-value
     String BI_PREPARE_PARTS_TIMEVALUE = "time-value";
     String BI_PREPARE_PARTS_TIMEVALUE_DESC = "long value of the time column";
 
     // time-format STRF_FORMAT; default=auto detect
     String BI_PREPARE_PARTS_TIMEFORMAT = "time-format";
     String BI_PREPARE_PARTS_TIMEFORMAT_DESC = "STRF_FORMAT; strftime(3) format of the time column";
 
     // output DIR
     String BI_PREPARE_PARTS_OUTPUTDIR = "output";
     String BI_PREPARE_PARTS_OUTPUTDIR_DEFAULTVALUE = "out"; // './out/'
     String BI_PREPARE_PARTS_OUTPUTDIR_DESC = "output directory";
 
     // error handling
     String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING = "error-records-handling";
     String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DEFAULTVALUE= "skip";
     String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DESC = "error records handling mode [skip, abort]; default=skip";
 
     // dry-run; show samples as JSON and exit
     String BI_PREPARE_PARTS_DRYRUN = "td.bulk_import.prepare_parts.dry-run";
     String BI_PREPARE_PARTS_DRYRUN_DEFAULTVALUE = "false";
 
     String BI_PREPARE_PARTS_SPLIT_SIZE = "split-size";
     String BI_PREPARE_PARTS_SPLIT_SIZE_DEFAULTVALUE ="16384";
     String BI_PREPARE_PARTS_SPLIT_SIZE_DESC = "size of each parts (default: 16384)";
 
     ////////////////////////////////////////
     // CSV/TSV PREPARE_PARTS_OPTIONS      //
     ////////////////////////////////////////
 
     // quote [DOUBLE, SINGLE]; default=DOUBLE
     String BI_PREPARE_PARTS_QUOTE = "quote";
     String BI_PREPARE_PARTS_QUOTE_DEFAULTVALUE = "DOUBLE";
     String BI_PREPARE_PARTS_QUOTE_DESC = "quote [DOUBLE, SINGLE]; default=DOUBLE";
 
     // delimiter CHAR; default=',' at 'csv', '\t' at 'tsv'
     String BI_PREPARE_PARTS_DELIMITER = "delimiter";
     String BI_PREPARE_PARTS_DELIMITER_CSV_DEFAULTVALUE = ",";
     String BI_PREPARE_PARTS_DELIMITER_TSV_DEFAULTVALUE = "\t";
     String BI_PREPARE_PARTS_DELIMITER_DESC = "delimiter CHAR; default=\",\" at csv, \"\\t\" at tsv";
 
     // newline [CRLF, LF, CR]; default=CRLF (or auto detect?)
     String BI_PREPARE_PARTS_NEWLINE = "newline";
     String BI_PREPARE_PARTS_NEWLINE_DEFAULTVALUE = "CRLF"; // default CRLF
     String BI_PREPARE_PARTS_NEWLINE_DESC = "newline [CRLR, LR, CR];  default=CRLF";
 
     // column-header; default=true
     String BI_PREPARE_PARTS_COLUMNHEADER = "column-header";
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
     String BI_PREPARE_PARTS_JDBC_CONNECTION_URL = "td.bulk_import_prepare_parts.jdbc.connection.url";
 
     // table
     String BI_PREPARE_PARTS_JDBC_TABLE = "td.bulk_import_prepare_parts.jdbc.table";
 
     // user
     String BI_PREPARE_PARTS_JDBC_USER = "td.bulk_import_prepare_parts.jdbc.user";
 
     // password
     String BI_PREPARE_PARTS_JDBC_PASSWORD = "td.bulk_import_prepare_parts.jdbc.password";
 }
